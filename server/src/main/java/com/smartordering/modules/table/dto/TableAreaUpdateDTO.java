package com.smartordering.modules.table.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 桌台区域更新 DTO
 *

 */
@Data
public class TableAreaUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 区域ID
     */
    @NotNull(message = "区域ID不能为空")
    private Long id;

    /**
     * 区域名称
     */
    @Size(max = 50, message = "区域名称长度不能超过50")
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态（0禁用 1启用）
     */
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 200, message = "备注长度不能超过200")
    private String remark;
}
