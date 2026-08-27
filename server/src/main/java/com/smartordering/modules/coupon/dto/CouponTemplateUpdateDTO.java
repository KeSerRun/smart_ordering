package com.smartordering.modules.coupon.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 更新优惠券模板 DTO
 *

 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CouponTemplateUpdateDTO extends CouponTemplateCreateDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    @NotNull(message = "模板ID不能为空")
    private Long id;
}
