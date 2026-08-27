package com.smartordering.modules.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * Order entity (order is MySQL reserved word, wrapped in backticks)
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`order`")
public class Order extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String orderNo;
    private Long tableId;
    private String tableCode;
    private String tableSessionCode;
    private BigDecimal originalAmount;
    private BigDecimal discountRate;
    private Long couponId;
    private String couponName;
    private Integer couponType;
    private BigDecimal couponThresholdAmount;
    private BigDecimal couponDiscountAmount;
    private BigDecimal couponDiscountRate;
    private BigDecimal actualAmount;
    private Integer pointsUsed;
    private BigDecimal pointsDiscountAmount;
    private BigDecimal paidAmount;
    /** Status: 0=pending payment 1=paid 2=cancelled 3=refunded */
    private Integer status;
    /** Payment mode: 0=pay first 1=pay after */
    private Integer paymentMode;
    /** Order type: 0=dine-in 1=takeaway */
    private Integer orderType;
    private String remark;
    private String customerOpenid;
}