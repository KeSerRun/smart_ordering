package com.smartordering.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典类型更新 DTO
 *

 */
@Data
public class DictTypeUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典类型ID
     */
    @NotNull(message = "字典类型ID不能为空")
    private Long id;

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称长度不能超过100")
    private String name;

    /**
     * 字典编码
     */
    @NotBlank(message = "字典编码不能为空")
    @Size(max = 100, message = "字典编码长度不能超过100")
    private String code;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
