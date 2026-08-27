package com.smartordering.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典数据创建 DTO
 *

 */
@Data
public class DictDataCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典类型ID
     */
    @NotNull(message = "字典类型ID不能为空")
    private Long typeId;

    /**
     * 字典标签
     */
    @NotBlank(message = "字典标签不能为空")
    @Size(max = 100, message = "字典标签长度不能超过100")
    private String label;

    /**
     * 字典值
     */
    @NotBlank(message = "字典值不能为空")
    @Size(max = 100, message = "字典值长度不能超过100")
    private String value;

    /**
     * 排序
     */
    private Integer orderNum = 0;

    /**
     * 状态
     */
    private Integer status = 1;

    /**
     * 备注
     */
    private String remark;
}
