package com.smartordering.modules.dish.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * Dish entity
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dish")
public class Dish extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Category ID */
    private Long categoryId;

    /** Dish name */
    private String name;

    /** Price */
    private BigDecimal price;

    /** Image URL */
    private String image;

    /** Thumbnail URL */
    private String thumbnail;

    /** Spice level: 0=none 1=mild 2=medium 3=hot */
    private Integer spiceLevel;

    /** Spec values (JSON) */
    private String specValues;

    /** Ingredients (JSON array) */
    private String ingredients;

    /** Description */
    private String description;

    /** Status: 0=off shelf 1=on sale */
    private Integer status;

    /** Sold out: 0=no 1=yes */
    private Integer soldOut;

    /** Stock: -1=unlimited */
    private Integer stock;

    /** Preparation time (minutes) */
    private Integer preparationTime;
}