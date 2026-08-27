package com.smartordering.modules.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order item view object
 *
 * @author smartordering
 */
@Data
public class OrderItemVO {

    private Long id;
    private Long orderId;
    private Long dishId;
    private String dishName;
    private String dishImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal amount;
    private String remark;
    private Integer status;
    private Integer paymentStatus;
    private Integer isGift;
    private LocalDateTime addedAt;
}