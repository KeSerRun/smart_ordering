package com.smartordering.modules.mq.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.framework.config.RabbitMqConfig;
import com.smartordering.modules.mq.dto.MqMessageQueryDTO;
import com.smartordering.modules.mq.entity.MqConsumeLog;
import com.smartordering.modules.mq.entity.MqMessage;
import com.smartordering.modules.mq.enums.MqConsumeStatus;
import com.smartordering.modules.mq.enums.MqMessageStatus;
import com.smartordering.modules.mq.mapper.MqConsumeLogMapper;
import com.smartordering.modules.mq.mapper.MqMessageMapper;
import com.smartordering.modules.mq.service.ReliableMessageService;
import com.smartordering.modules.mq.vo.MqMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Reliable message service implementation
 * <p>
 * 可靠消息的「事务性发件箱」实现：
 * 1. {@link #send} 在业务事务内写 {@code mq_message}（deliver_status=0），
 *    事务提交后通过 {@link TransactionSynchronization#afterCommit()} 立即投递；
 * 2. 投递失败保留待投递状态并写入 last_error / next_retry_time，
 *    由 {@code ReliableMessageResendTask} 定时扫描补偿重发；
 * 3. 消费端用 {@code mq_consume_log} 唯一键（consumer_group, message_key）保证幂等。
 * </p>
 * <p>单节点部署假设：未加分布式锁，多实例部署时补偿任务应加锁避免重复投递。</p>
 *
 * @author smartordering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReliableMessageServiceImpl implements ReliableMessageService {

    private static final long MAX_ERROR_LENGTH = 500;

    private final MqMessageMapper mqMessageMapper;
    private final MqConsumeLogMapper mqConsumeLogMapper;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqConfig rabbitMqConfig;
    private final ObjectMapper objectMapper;

    /** 最大投递重试次数，超过后标记为投递失败（FAILED） */
    @Value("${smart.mq.max-retry-count:5}")
    private int maxRetryCount;

    /**
     * 投递线程池：事务提交后的实际发布在独立线程执行，
     * 即使 RabbitMQ 短暂不可用，也不阻塞下单等业务请求线程；
     * 失败的消息仍留在发件箱，由定时补偿任务兜底重发。
     */
    private final ExecutorService publishExecutor =
            Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "mq-publish");
                t.setDaemon(true);
                return t;
            });

    @Override
    public IPage<MqMessageVO> pageMessages(MqMessageQueryDTO dto) {
        LambdaQueryWrapper<MqMessage> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getBizType())) {
            wrapper.eq(MqMessage::getBizType, dto.getBizType());
        }
        if (StringUtils.hasText(dto.getMessageKey())) {
            wrapper.eq(MqMessage::getMessageKey, dto.getMessageKey());
        }
        if (dto.getDeliverStatus() != null) {
            wrapper.eq(MqMessage::getDeliverStatus, dto.getDeliverStatus());
        }
        wrapper.orderByDesc(MqMessage::getCreateTime);

        Page<MqMessage> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        IPage<MqMessage> result = mqMessageMapper.selectPage(page, wrapper);
        return result.convert(this::toVO);
    }

    @Override
    public void retryMessage(Long id) {
        MqMessage message = mqMessageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException("Message not found");
        }
        // Reset to pending delivery
        message.setDeliverStatus(MqMessageStatus.PENDING.getCode());
        message.setRetryCount(0);
        message.setNextRetryTime(LocalDateTime.now());
        message.setLastError(null);
        mqMessageMapper.updateById(message);
        // 重置后立即尝试一次投递，不必等定时补偿周期
        publishPending(id);
    }

    @Override
    @Transactional
    public void send(String messageKey, String topic, String tag, String bizType, String bizKey, Object payload) {
        if (!StringUtils.hasText(messageKey)) {
            messageKey = bizType + ":" + bizKey + ":" + IdUtil.getSnowflakeNextIdStr();
        }
        if (!StringUtils.hasText(bizType) || !StringUtils.hasText(bizKey)) {
            throw new BusinessException("bizType and bizKey are required");
        }
        MqMessage message = new MqMessage();
        message.setMessageKey(messageKey);
        message.setTopic(topic);
        message.setTag(tag);
        message.setBizType(bizType);
        message.setBizKey(bizKey);
        message.setPayload(toJson(payload));
        message.setDeliverStatus(MqMessageStatus.PENDING.getCode());
        message.setRetryCount(0);
        message.setNextRetryTime(LocalDateTime.now());
        mqMessageMapper.insert(message);

        Long messageId = message.getId();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // 业务事务内：事务提交后再投递，避免半成品消息外发；异步执行不阻塞业务请求
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishExecutor.submit(() -> publishPending(messageId));
                }
            });
        } else {
            // 无事务调用：直接异步投递（发件箱兜底）
            publishExecutor.submit(() -> publishPending(messageId));
        }
        log.info("Reliable message written to outbox: id={}, key={}, topic={}", messageId, message.getMessageKey(), topic);
    }

    @Override
    public void publishPending(Long messageId) {
        MqMessage message = mqMessageMapper.selectById(messageId);
        if (message == null) {
            log.warn("Reliable message not found, skip publish: id={}", messageId);
            return;
        }
        if (MqMessageStatus.SENT.getCode() == message.getDeliverStatus()) {
            return; // 已投递，幂等跳过
        }
        try {
            rabbitTemplate.convertAndSend(
                    rabbitMqConfig.getTopicExchange(), message.getTopic(), message.getPayload());
            MqMessage update = new MqMessage();
            update.setId(message.getId());
            update.setDeliverStatus(MqMessageStatus.SENT.getCode());
            update.setSentTime(LocalDateTime.now());
            update.setLastError(null);
            mqMessageMapper.updateById(update);
            log.info("Reliable message published: id={}, key={}, topic={}", message.getId(), message.getMessageKey(), message.getTopic());
        } catch (Exception e) {
            handlePublishFailure(message, e);
        }
    }

    @Override
    @Transactional
    public Long startConsume(String consumerGroup, String topic, String tag, String messageKey, String bizKey) {
        MqConsumeLog consumeLog = new MqConsumeLog();
        consumeLog.setConsumerGroup(consumerGroup);
        consumeLog.setTopic(topic);
        consumeLog.setTag(tag);
        consumeLog.setMessageKey(messageKey);
        consumeLog.setBizKey(bizKey);
        consumeLog.setConsumeStatus(MqConsumeStatus.INIT.getCode());
        consumeLog.setRetryCount(0);
        try {
            mqConsumeLogMapper.insert(consumeLog);
            return consumeLog.getId();
        } catch (DuplicateKeyException e) {
            // (consumer_group, message_key) 唯一键冲突 = 已消费过 → 幂等跳过
            log.info("Message already consumed, skip: group={}, key={}", consumerGroup, messageKey);
            return null;
        }
    }

    @Override
    public void finishConsume(Long consumeLogId, boolean success, String error) {
        if (consumeLogId == null) {
            return;
        }
        MqConsumeLog update = new MqConsumeLog();
        update.setId(consumeLogId);
        update.setConsumeStatus(success ? MqConsumeStatus.SUCCESS.getCode() : MqConsumeStatus.FAILED.getCode());
        update.setFinishedTime(LocalDateTime.now());
        if (!success) {
            update.setLastError(truncate(error));
        }
        mqConsumeLogMapper.updateById(update);
        if (success) {
            log.debug("Consume finished: id={}, status=SUCCESS", consumeLogId);
        } else {
            log.warn("Consume finished: id={}, status=FAILED, error={}", consumeLogId, truncate(error));
        }
    }

    private void handlePublishFailure(MqMessage message, Exception e) {
        int currentRetry = message.getRetryCount() == null ? 0 : message.getRetryCount();
        MqMessage update = new MqMessage();
        update.setId(message.getId());
        update.setLastError(truncate(e.getMessage()));
        if (currentRetry >= maxRetryCount) {
            update.setDeliverStatus(MqMessageStatus.FAILED.getCode());
            log.error("Reliable message publish failed, exceeded max retry {}: id={}, key={}",
                    maxRetryCount, message.getId(), message.getMessageKey(), e);
        } else {
            int nextRetry = currentRetry + 1;
            update.setRetryCount(nextRetry);
            update.setNextRetryTime(LocalDateTime.now().plusSeconds(backoffSeconds(nextRetry)));
            log.warn("Reliable message publish failed, scheduled retry #{}: id={}, key={}, next={}",
                    nextRetry, message.getId(), message.getMessageKey(), update.getNextRetryTime(), e);
        }
        mqMessageMapper.updateById(update);
    }

    /** 指数退避：15s * 2^(retry-1)，封顶 300s */
    private long backoffSeconds(int retry) {
        return Math.min(300L, 15L * (1L << Math.min(retry - 1, 5)));
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Failed to serialize mq payload: " + e.getMessage());
        }
    }

    private String truncate(String error) {
        if (!StringUtils.hasText(error)) {
            return "unknown error";
        }
        return error.length() <= MAX_ERROR_LENGTH
                ? error
                : error.substring(0, (int) MAX_ERROR_LENGTH);
    }

    private MqMessageVO toVO(MqMessage message) {
        MqMessageVO vo = new MqMessageVO();
        BeanUtils.copyProperties(message, vo);
        return vo;
    }
}