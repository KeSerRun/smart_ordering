package com.smartordering.modules.dish.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Dish category entity
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dish_category")
public class DishCategory extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Category name */
    private String name;

    /** Sort order */
    private Integer sort;

    /** Status: 0=disabled 1=enabled */
    private Integer status;

    /** Category image URL */
    private String image;

    /** Spec template: 0=none 1=spice 2=drink */
    private Integer specTemplate;
}