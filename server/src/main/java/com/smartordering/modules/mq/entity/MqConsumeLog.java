package com.smartordering.modules.mq.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * MQ 消费日志实体
 *
 * <p>用于消费者幂等：{@code (consumer_group, message_key)} 有数据库唯一键，
 * 同一消息被重复投递时第二次插入会撞唯一键，直接跳过，避免业务重复处理。</p>
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mq_consume_log")
public class MqConsumeLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 消费者组（如 kitchen） */
    private String consumerGroup;

    /** 路由键 / 主题 */
    private String topic;

    /** 标签（如 NEW_ORDER） */
    private String tag;

    /** 消息唯一键，与 mq_message.message_key 一致 */
    private String messageKey;

    /** 业务主键（如订单ID） */
    private String bizKey;

    /** 消费状态：0 待消费 1 成功 2 失败 */
    private Integer consumeStatus;

    /** 重试次数 */
    private Integer retryCount;

    /** 最后错误信息 */
    private String lastError;

    /** 完成时间 */
    private LocalDateTime finishedTime;
}