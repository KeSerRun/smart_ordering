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
 * 周期性扫描并重新投递待处理的消息，确保消息最终投递成功。
 * 解决事务提交后首次投递失败、或应用崩溃导致状态未回写等场景下的消息丢失问题。
 *
 * 扫描条件：
 * - 投递状态为待投递（deliver_status = 0）
 * - 下次重试时间已到（next_retry_time <= now）
 * - 重试次数未达上限（retry_count < maxRetryCount）
 *
 * 注意事项：
 * - 单次最多扫描 100 条消息，避免单次任务占用过多资源
 * - 逐条调用 ReliableMessageService.publishPending(Long) 进行实际投递
 * - 部署约束：假设单节点部署，多实例部署需引入分布式锁防止重复投递
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

    // 间隔为 15s 的定时任务
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