package com.smartordering.modules.table.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 桌台创建 DTO
 *

 */
@Data
public class TableCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 桌台编号（唯一）
     */
    @NotBlank(message = "桌台编号不能为空")
    @Size(max = 50, message = "桌台编号长度不能超过50")
    private String code;

    /**
     * 桌台名称
     */
    @NotBlank(message = "桌台名称不能为空")
    @Size(max = 50, message = "桌台名称长度不能超过50")
    private String name;

    /**
     * 座位数
     */
    private Integer capacity;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称（兼容历史调用）
     */
    @Size(max = 50, message = "区域名称长度不能超过50")
    private String areaName;
}
