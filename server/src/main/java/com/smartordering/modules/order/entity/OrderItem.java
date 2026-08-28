package com.smartordering.modules.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order item entity
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_item")
public class OrderItem extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private Long dishId;
    private String dishName;
    private String dishImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal amount;
    private String remark;
    /** Status: 0=pending 1=cooking 2=done */
    private Integer status;
    /** 上菜状态：0未上菜 1已上菜 */
    private Integer serveStatus;
    /** Payment status: 0=unpaid 2=paid */
    private Integer paymentStatus;
    /** Is gift: 0=no 1=yes */
    private Integer isGift;
    private LocalDateTime addedAt;
}