package com.smartordering.modules.dish.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Dish spec option entity (a selectable value inside a spec group).
 *
 * @author smartordering
 */
@Data
@TableName("dish_spec_option")
public class DishSpecOption implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Snowflake ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** Owning spec group ID */
    private Long groupId;

    /** Option name */
    private String name;

    /** Sort order */
    private Integer sort;

    /** Price delta applied when this option is selected (positive = extra charge, negative = discount) */
    private BigDecimal price;

    /** Logical delete: 0=normal, 1=deleted */
    @TableLogic
    @TableField
    private Integer deleted;
}