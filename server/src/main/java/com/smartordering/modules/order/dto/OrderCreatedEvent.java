package com.smartordering.modules.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 新订单创建事件载荷
 *
 * <p>下单成功后写入发件箱并经 RabbitMQ 广播（routing key: order.created），
 * 后厨消费者据此触发 WebSocket 大屏刷新。payload 即本对象 JSON。</p>
 *
 * @author smartordering
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 消息唯一键，与 mq_message.message_key 一致（消费幂等依赖） */
    private String messageKey;

    private Long orderId;

    private String orderNo;

    private Long tableId;

    private String tableCode;

    /** 订单类型：0 堂食 1 外带 */
    private Integer orderType;

    /** 支付方式：0 先付 1 后付 */
    private Integer paymentMode;

    /** 订单状态：0 待支付 1 已支付 2 已取消 3 已退款 */
    private Integer status;

    private BigDecimal originalAmount;

    private BigDecimal actualAmount;

    private String remark;

    /** 下单用户 ID（BaseEntity.createBy） */
    private Long userId;

    private LocalDateTime createdAt;

    /** 订单明细 */
    private List<Item> items;

    /**
     * 订单项快照
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Long dishId;

        private String dishName;

        private Integer quantity;

        private BigDecimal amount;

        private String remark;
    }
}