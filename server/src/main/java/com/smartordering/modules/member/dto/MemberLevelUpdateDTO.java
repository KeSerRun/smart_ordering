package com.smartordering.modules.member.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员等级更新 DTO
 *

 */
@Data
public class MemberLevelUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "等级名称不能为空")
    private String levelName;

    @NotNull(message = "排序不能为空")
    @Min(value = 0, message = "排序不能小于0")
    private Integer sort;

    @NotNull(message = "成长值门槛不能为空")
    @Min(value = 0, message = "成长值门槛不能小于0")
    private Integer growthThreshold;

    @NotNull(message = "积分倍率不能为空")
    private BigDecimal pointsRate;

    @NotNull(message = "折扣倍率不能为空")
    private BigDecimal discountRate;

    private String benefitConfig;

    private Long upgradeCouponTemplateId;

    private Long exclusiveCouponTemplateId;

    private String remark;

    private Integer status;
}
