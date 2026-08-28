package com.smartordering.modules.mq.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartordering.modules.mq.dto.MqMessageQueryDTO;
import com.smartordering.modules.mq.vo.MqMessageVO;

/**
 * 可靠消息服务
 *
 * <p>采用「事务性发件箱（Transactional Outbox） + 提交后发布 + 定时补偿」：
 * 业务事务内只写 {@code mq_message}（bozType/bizKey/payload），
 * 事务提交后把消息发布到 RabbitMQ；发布失败不阻塞业务，
 * 由 {@code ReliableMessageResendTask} 定时扫描待投递消息补偿重发。</p>
 *
 * @author smartordering
 */
public interface ReliableMessageService {

    IPage<MqMessageVO> pageMessages(MqMessageQueryDTO dto);

    void retryMessage(Long id);

    /**
     * 事务性发件箱：在当前事务内写入一条待投递消息，事务提交后自动尝试发布到 RabbitMQ。
     * <p>发布成功标记为已投递；发布失败保留待投递状态，由定时补偿任务重发。
     * 若业务事务回滚，本条消息随之消失，不会产生孤消息。</p>
     *
     * @param messageKey 消息唯一键（全表唯一，消费端幂等依赖），建议同时写入 payload
     * @param topic      路由键 / 主题（如 order.created）
     * @param tag        标签（如 NEW_ORDER）
     * @param bizType    业务类型（如 ORDER）
     * @param bizKey     业务主键（如 orderId）
     * @param payload    业务载荷，会被序列化为 JSON 存入 payload 并原样发出
     */
    void send(String messageKey, String topic, String tag, String bizType, String bizKey, Object payload);

    /**
     * 立即对一条消息执行发布（重选最新状态后投递）。
     * <p>供定时补偿任务调用：扫描到 {@code deliver_status=0 且 next_retry_time<=now}
     * 的消息后逐个调用本方法完成实际投递。</p>
     *
     * @param messageId mq_message 主键
     */
    void publishPending(Long messageId);

    /**
     * 消费幂等入口：写入消费日志占位（唯一键 {@code consumer_group + message_key}）。
     * <p>若该消息已被本消费者组消费过，第二次插入撞唯一键，返回 {@code null}，调用方应跳过处理。</p>
     *
     * @return 消费日志 id；重复消息返回 {@code null}
     */
    Long startConsume(String consumerGroup, String topic, String tag, String messageKey, String bizKey);

    /**
     * 结束消费：更新消费日志状态与完成时间。
     *
     * @param consumeLogId startConsume 返回的日志 id
     * @param success      是否处理成功
     * @param error        失败时的错误信息
     */
    void finishConsume(Long consumeLogId, boolean success, String error);
}