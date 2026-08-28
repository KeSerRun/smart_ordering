package com.smartordering.modules.coupon.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartordering.modules.coupon.dto.CouponGrantEvent;
import com.smartordering.modules.coupon.service.CouponService;
import com.smartordering.modules.mq.service.ReliableMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 发券任务消费者
 *
 * <p>监听 RabbitMQ 的 {@code coupon.grant.queue}（routing key: coupon.grant.create），
 * 收到「发放优惠券」任务后异步执行：</p>
 * <ol>
 *   <li>先经 {@link ReliableMessageService#startConsume} 用 mq_consume_log 唯一键幂等去重，
 *       重复投递直接跳过，不重复发券；</li>
 *   <li>调用 {@link CouponService#executeGrantTask} 按任务画像（全部用户 / 指定用户 / 按会员等级）
 *       逐人发券并更新任务计数；</li>
 *   <li>失败不重抛（避免无限重投），任务状态在 service 内更新为 FAILED，记 failure 到 mq_consume_log。</li>
 * </ol>
 *
 * @author smartordering
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponGrantConsumer {

    /** 消费者组：发券任务 */
    private static final String CONSUMER_GROUP = "coupon-grant";

    private final ObjectMapper objectMapper;
    private final ReliableMessageService reliableMessageService;
    private final CouponService couponService;

    @RabbitListener(queues = "${smart.mq.coupon-grant-queue}")
    public void onGrant(String payload) {
        CouponGrantEvent event = parsePayload(payload);
        if (event == null || event.getTaskId() == null) {
            log.warn("Ignored invalid coupon grant event payload: {}", payload);
            return;
        }

        Long consumeId = reliableMessageService.startConsume(
                CONSUMER_GROUP,
                "coupon.grant.create",
                "GRANT_COUPON",
                event.getMessageKey(),
                String.valueOf(event.getTaskId()));
        if (consumeId == null) {
            // 幂等：该消息已被本消费者组处理过，跳过
            log.debug("Coupon grant event already consumed, skip: messageKey={}", event.getMessageKey());
            return;
        }

        try {
            int done = couponService.executeGrantTask(event.getTaskId());
            reliableMessageService.finishConsume(consumeId, true, null);
            log.info("Coupon grant consumed: taskId={}, granted={}", event.getTaskId(), done);
        } catch (Exception e) {
            log.error("Coupon grant consume failed: taskId={}", event.getTaskId(), e);
            reliableMessageService.finishConsume(consumeId, false, e.getMessage());
        }
    }

    private CouponGrantEvent parsePayload(String payload) {
        try {
            return objectMapper.readValue(payload, CouponGrantEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse coupon grant event payload", e);
            return null;
        }
    }
}