package com.smartordering.modules.coupon.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发券任务事件（写 mq_message 发件箱，RabbitMQ 异步投递给消费者）
 *
 * @author smartordering
 */
@Data
@Builder
public class CouponGrantEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 消息键（= 任务ID，幂等去重用） */
    private String messageKey;

    /** 发券任务ID */
    private Long taskId;
}