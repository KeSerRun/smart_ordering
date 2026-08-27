package com.smartordering.modules.coupon.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * User coupon view object
 *
 * @author smartordering
 */
@Data
public class UserCouponVO {

    private Long id;
    private String couponName;
    private Integer couponType;
    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    /** Status: 0=unused 1=used 2=expired 3=locked */
    private Integer status;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private LocalDateTime receivedTime;
}