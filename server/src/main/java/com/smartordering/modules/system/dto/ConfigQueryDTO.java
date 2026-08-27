package com.smartordering.modules.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 配置查询 DTO
 *

 */
@Data
public class ConfigQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}
