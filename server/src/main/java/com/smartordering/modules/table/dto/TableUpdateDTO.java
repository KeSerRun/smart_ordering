package com.smartordering.modules.table.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 桌台更新 DTO
 *

 */
@Data
public class TableUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 桌台ID
     */
    @NotNull(message = "桌台ID不能为空")
    private Long id;

    /**
     * 桌台编号
     */
    @Size(max = 50, message = "桌台编号长度不能超过50")
    private String code;

    /**
     * 桌台名称
     */
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
