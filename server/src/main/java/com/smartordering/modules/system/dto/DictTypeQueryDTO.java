package com.smartordering.modules.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典类型查询 DTO
 *

 */
@Data
public class DictTypeQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典名称
     */
    private String name;

    /**
     * 字典编码
     */
    private String code;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}
