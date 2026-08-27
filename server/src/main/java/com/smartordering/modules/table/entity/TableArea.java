package com.smartordering.modules.table.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Dining table area entity.
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("table_area")
public class TableArea extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Area name */
    private String name;

    /** Sort order */
    private Integer sort;

    /** Status: 0=disabled 1=enabled */
    private Integer status;

    /** Remark */
    private String remark;
}