package com.smartordering.modules.mq.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartordering.modules.mq.entity.MqMessage;
import com.smartordering.modules.mq.enums.MqMessageStatus;
import com.smartordering.modules.mq.mapper.MqMessageMapper;
import com.smartordering.modules.mq.service.ReliableMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 可靠消息定时补偿任务
 *
 * <p>周期性扫描 {@code mq_message} 中达成重发条件的待投递消息：
 * {@code deliver_status=0 且 next_retry_time<=now 且 retry_count<maxRetryCount}，
 * 逐条调用 {@link ReliableMessageService#publishPending} 实际投递。</p>
 *
 * <p>事务提交后首次投递失败、或发布后应用崩溃导致未回写状态的消息，都会在此兜底补偿。
 * 单节点部署假设：不做分布式锁，多实例部署需加锁防重复投递。</p>
 *
 * @author smartordering
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReliableMessageResendTask {

    private final MqMessageMapper mqMessageMapper;
    private final ReliableMessageService reliableMessageService;

    @Value("${smart.mq.max-retry-count:5}")
    private int maxRetryCount;

    @Scheduled(fixedDelayString = "${smart.mq.resend-interval-ms:15000}")
    public void resendPendingMessages() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<MqMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MqMessage::getDeliverStatus, MqMessageStatus.PENDING.getCode())
                .le(MqMessage::getNextRetryTime, now)
                .lt(MqMessage::getRetryCount, maxRetryCount)
                .last("LIMIT 100");
        List<MqMessage> pending = mqMessageMapper.selectList(wrapper);
        if (pending.isEmpty()) {
            return;
        }
        log.info("Reliable message resend task finds {} pending message(s)", pending.size());
        for (MqMessage message : pending) {
            reliableMessageService.publishPending(message.getId());
        }
    }
}