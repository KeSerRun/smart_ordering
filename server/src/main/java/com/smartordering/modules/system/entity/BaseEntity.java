package com.smartordering.modules.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base entity with common fields: id, createBy, updateBy, createTime, updateTime, deleted
 *
 * @author smartordering
 */
@Data
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Snowflake ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** Creator user ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** Updater user ID */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** Create time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** Update time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** Logical delete: 0=normal, 1=deleted */
    @TableLogic
    private Integer deleted;
}