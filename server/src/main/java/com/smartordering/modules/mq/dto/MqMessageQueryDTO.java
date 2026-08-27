package com.smartordering.modules.mq.dto;

import lombok.Data;

/**
 * MQ message query DTO
 *
 * @author smartordering
 */
@Data
public class MqMessageQueryDTO {

    private String bizType;
    private String messageKey;
    private Integer deliverStatus;
    private Long pageNum = 1L;
    private Long pageSize = 10L;
}