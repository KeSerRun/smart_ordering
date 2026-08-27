package com.smartordering.modules.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色查询 DTO
 *

 */
@Data
public class RoleQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 页码
     */
    private Long pageNum = 1L;

    /**
     * 每页数量
     */
    private Long pageSize = 10L;
}
