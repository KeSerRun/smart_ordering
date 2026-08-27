package com.smartordering.modules.dish.dto;

import lombok.Data;

import java.util.List;

/**
 * Dish category create/update payload. {@code id} is present when updating.
 *
 * @author smartordering
 */
@Data
public class DishCategoryAdminDTO {

    /** Present when updating */
    private Long id;

    private String name;

    private Integer sort;

    private Integer status;

    private String image;

    /** Bound spec group IDs */
    private List<Long> specGroupIds;
}