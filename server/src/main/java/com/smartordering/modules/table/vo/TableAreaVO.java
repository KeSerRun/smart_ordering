package com.smartordering.modules.table.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 桌台区域 VO
 *

 */
@Data
public class TableAreaVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 区域ID
     */
    private Long id;

    /**
     * 区域名称
     */
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
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
