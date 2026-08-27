package com.smartordering.modules.mq.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * MQ message view object
 *
 * @author smartordering
 */
@Data
public class MqMessageVO {

    private Long id;
    private String messageKey;
    private String topic;
    private String tag;
    private String bizType;
    private String bizKey;
    private Integer deliverStatus;
    private Integer retryCount;
    private LocalDateTime nextRetryTime;
    private String lastError;
    private LocalDateTime sentTime;
    private LocalDateTime createTime;
}