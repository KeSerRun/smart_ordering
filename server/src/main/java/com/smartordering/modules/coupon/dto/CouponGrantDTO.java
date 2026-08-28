package com.smartordering.modules.coupon.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 发券 DTO
 *
 * @author smartordering
 */
@Data
public class CouponGrantDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    /**
     * 发放方式（1指定用户 2全部用户 3按会员等级）
     */
    @NotNull(message = "发放方式不能为空")
    @Min(value = 1, message = "发放方式不正确")
    @Max(value = 3, message = "发放方式不正确")
    private Integer grantMode;

    /**
     * 指定用户ID列表（grantMode=1 时必填）
     */
    private List<Long> userIds;

    /**
     * 指定会员等级ID列表（grantMode=3 时必填，只发给这些等级下的会员）
     */
    private List<Long> levelIds;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
}