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

    /** 用户券ID */
    private Long id;

    /** 券模板ID */
    private Long templateId;

    /** 持券用户ID */
    private Long userId;

    /** 持券用户名 */
    private String username;

    /** 持券用户昵称 */
    private String nickname;

    /** 持券用户手机号 */
    private String phone;

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