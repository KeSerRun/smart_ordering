package com.smartordering.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单更新 DTO
 *

 */
@Data
public class MenuUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜单ID
     */
    @NotNull(message = "菜单ID不能为空")
    private Long id;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50")
    private String name;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 菜单类型（0-目录 1-菜单 2-按钮）
     */
    @NotNull(message = "菜单类型不能为空")
    private Integer type;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer orderNum;

    /**
     * 状态
     */
    private Integer status;
}
