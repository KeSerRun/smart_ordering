package com.smartordering.modules.coupon.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Coupon template view object.
 *
 * @author smartordering
 */
@Data
public class CouponTemplateVO {

    private Long id;
    private String name;
    private Integer type;
    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    private Integer totalQuantity;
    private Integer issuedQuantity;
    private Integer perUserLimit;
    private Integer validityType;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer validDays;
    private Integer status;
    private String description;
    private String availableWeekdays;
    private LocalDateTime createTime;
}