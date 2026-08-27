package com.smartordering.modules.system.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典类型 VO
 *

 */
@Data
public class DictTypeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典类型ID
     */
    private Long id;

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
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
