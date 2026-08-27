package com.smartordering.modules.dish.vo;

import lombok.Data;

/**
 * Dish category view object
 *
 * @author smartordering
 */
@Data
public class DishCategoryVO {

    private Long id;
    private String name;
    private Integer sort;
    private String image;
}