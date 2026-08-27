package com.smartordering.modules.banner.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 首页轮播图创建 DTO
 *

 */
@Data
public class HomeBannerCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主标题（PROFILE_HERO 场景允许为空）
     */
    private String title;

    /**
     * 副标题
     */
    private String subtitle;

    /**
     * 图片地址
     */
    @NotBlank(message = "图片地址不能为空")
    private String imageUrl;

    /**
     * 操作类型
     */
    @NotNull(message = "操作类型不能为空")
    @Min(value = 0, message = "操作类型不正确")
    @Max(value = 2, message = "操作类型不正确")
    private Integer actionType;

    /**
     * 跳转路径
     */
    private String targetPath;

    /**
     * 投放位置
     */
    @NotBlank(message = "投放位置不能为空")
    private String scene;

    /**
     * 排序
     */
    @NotNull(message = "排序不能为空")
    private Integer sort;

    /**
     * 状态
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态不正确")
    @Max(value = 1, message = "状态不正确")
    private Integer status;
}
