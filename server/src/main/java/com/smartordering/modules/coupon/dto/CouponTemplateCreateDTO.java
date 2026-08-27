package com.smartordering.modules.coupon.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建优惠券模板 DTO
 *

 */
@Data
public class CouponTemplateCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    private String name;

    /**
     * 优惠券类型（1满减 2折扣）
     */
    @NotNull(message = "优惠券类型不能为空")
    @Min(value = 1, message = "优惠券类型不正确")
    @Max(value = 2, message = "优惠券类型不正确")
    private Integer type;

    /**
     * 使用门槛金额
     */
    @DecimalMin(value = "0.00", message = "使用门槛不能小于0")
    private BigDecimal thresholdAmount;

    /**
     * 优惠金额
     */
    @DecimalMin(value = "0.00", message = "优惠金额不能小于0")
    private BigDecimal discountAmount;

    /**
     * 折扣比例
     */
    @DecimalMin(value = "0.01", message = "折扣比例不能小于0.01")
    @DecimalMax(value = "0.99", message = "折扣比例不能大于0.99")
    private BigDecimal discountRate;

    /**
     * 发放总量
     */
    @NotNull(message = "发放总量不能为空")
    @Min(value = 0, message = "发放总量不能小于0")
    private Integer totalQuantity;

    /**
     * 每人限领张数
     */
    @NotNull(message = "每人限领不能为空")
    @Min(value = 0, message = "每人限领不能小于0")
    private Integer perUserLimit;

    /**
     * 有效期类型（1固定时间 2领券后N天）
     */
    @NotNull(message = "有效期类型不能为空")
    @Min(value = 1, message = "有效期类型不正确")
    @Max(value = 2, message = "有效期类型不正确")
    private Integer validityType;

    /**
     * 固定生效时间
     */
    private LocalDateTime validFrom;

    /**
     * 固定失效时间
     */
    private LocalDateTime validTo;

    /**
     * 领券后有效天数
     */
    @Min(value = 1, message = "有效天数不能小于1")
    private Integer validDays;

    /**
     * 状态
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态不正确")
    @Max(value = 1, message = "状态不正确")
    private Integer status;

    /**
     * 说明
     */
    @Size(max = 500, message = "说明长度不能超过500")
    private String description;

    /**
     * 可用星期，使用 1-7 表示周一到周日，多个值使用逗号分隔，空表示每天可用
     */
    @Size(max = 20, message = "可用星期配置长度不能超过20")
    private String availableWeekdays;
}
