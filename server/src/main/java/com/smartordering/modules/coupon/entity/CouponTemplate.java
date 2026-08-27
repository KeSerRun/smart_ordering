package com.smartordering.modules.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Coupon template entity
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon_template")
public class CouponTemplate extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    /** Type: 1=full reduction 2=discount */
    private Integer type;
    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    private Integer totalQuantity;
    private Integer issuedQuantity;
    private Integer perUserLimit;
    /** Validity type: 1=fixed time 2=N days after receive */
    private Integer validityType;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer validDays;
    private Integer status;
    private String description;
    private String availableWeekdays;
}