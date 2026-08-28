package com.smartordering.modules.mq.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartordering.common.enums.WsEventType;
import com.smartordering.framework.websocket.WsService;
import com.smartordering.modules.kitchen.service.KitchenService;
import com.smartordering.modules.kitchen.vo.KitchenTaskVO;
import com.smartordering.modules.mq.service.ReliableMessageService;
import com.smartordering.modules.order.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 新订单事件消费者（后厨组）
 *
 * <p>监听 RabbitMQ 的 {@code kitchen.order.queue}（routing key: order.created），
 * 消费「下单成功」事件并触发后厨大屏 WebSocket 实时刷新：</p>
 * <ol>
 *   <li>先经 {@link ReliableMessageService#startConsume} 用 mq_consume_log 唯一键幂等去重，
 *       重复投递直接跳过，ack 不重复处理；</li>
 *   <li>复用 {@link KitchenService#getTaskList()} 取全量待接/制作中任务（与 GET /app/kitchen/tasks 一致），
 *       通过 {@link WsService} 广播到 /topic/kitchen，admin 后厨大屏收到后整体替换任务列表。</li>
 * </ol>
 * <p>处理失败不重抛（避免无限重投），记 failure 到 mq_consume_log 供排查。</p>
 *
 * @author smartordering
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    /** 消费者组：后厨 */
    private static final String CONSUMER_GROUP = "kitchen";

    /** 后厨大屏 WebSocket 广播主题 */
    public static final String KITCHEN_TOPIC = "/topic/kitchen";

    private final ObjectMapper objectMapper;
    private final ReliableMessageService reliableMessageService;
    private final KitchenService kitchenService;
    private final WsService wsService;

    @RabbitListener(queues = "${smart.mq.kitchen-queue}")
    public void onOrderCreated(String payload) {
        OrderCreatedEvent event = parsePayload(payload);
        if (event == null || event.getOrderId() == null) {
            log.warn("Ignored invalid order event payload: {}", payload);
            return;
        }

        Long consumeId = reliableMessageService.startConsume(
                CONSUMER_GROUP,
                "order.created",
                "NEW_ORDER",
                event.getMessageKey(),
                String.valueOf(event.getOrderId()));
        if (consumeId == null) {
            // 幂等：该消息已被本消费者组处理过，跳过
            log.debug("Order event already consumed, skip: messageKey={}", event.getMessageKey());
            return;
        }

        try {
            // 自动接单：开关开启时，新订单的菜品任务直接置为制作中（无需后厨手动点接单）
            if (kitchenService.getAutoAcceptEnabled()) {
                int accepted = kitchenService.autoAcceptByOrder(event.getOrderId());
                if (accepted > 0) {
                    log.info("Kitchen auto-accept enabled, orderNo={}, accepted={}",
                            event.getOrderNo(), accepted);
                }
            }
            List<KitchenTaskVO> tasks = kitchenService.getTaskList();
            wsService.broadcast(WsEventType.NEW_ORDER, KITCHEN_TOPIC, tasks);
            reliableMessageService.finishConsume(consumeId, true, null);
            log.info("Order event consumed and pushed to kitchen: orderNo={}, messageKey={}",
                    event.getOrderNo(), event.getMessageKey());
        } catch (Exception e) {
            log.error("Order event consume failed: orderNo={}, messageKey={}",
                    event.getOrderNo(), event.getMessageKey(), e);
            reliableMessageService.finishConsume(consumeId, false, e.getMessage());
        }
    }

    private OrderCreatedEvent parsePayload(String payload) {
        try {
            return objectMapper.readValue(payload, OrderCreatedEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse order event payload", e);
            return null;
        }
    }
}