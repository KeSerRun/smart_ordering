package com.smartordering.modules.dish.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Dish spec group entity.
 *
 * <p>A spec group groups several options, e.g. "size" with 小份/大份,
 * or "spice" with 微辣/中辣/重辣.</p>
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dish_spec_group")
public class DishSpecGroup extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Group name */
    private String name;

    /** Sort order */
    private Integer sort;

    /** Status: 0=disabled 1=enabled */
    private Integer status;
}