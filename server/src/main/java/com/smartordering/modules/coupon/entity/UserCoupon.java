package com.smartordering.modules.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * User coupon entity
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_coupon")
public class UserCoupon extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long templateId;
    private Long userId;
    private String username;
    private String nickname;
    private String phone;
    private String couponName;
    private Integer couponType;
    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    private Integer sourceType;
    /** Status: 0=unused 1=used 2=expired 3=locked */
    private Integer status;
    private LocalDateTime receivedTime;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private LocalDateTime usedTime;
    private Long orderId;
    private Long grantTaskId;
    private String availableWeekdays;
}