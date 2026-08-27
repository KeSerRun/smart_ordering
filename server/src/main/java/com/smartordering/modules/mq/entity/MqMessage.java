package com.smartordering.modules.mq.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * MQ message entity
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mq_message")
public class MqMessage extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String messageKey;
    private String topic;
    private String tag;
    private String bizType;
    private String bizKey;
    private String payload;
    private Integer deliverStatus;
    private Integer retryCount;
    private LocalDateTime nextRetryTime;
    private String lastError;
    private LocalDateTime sentTime;
}