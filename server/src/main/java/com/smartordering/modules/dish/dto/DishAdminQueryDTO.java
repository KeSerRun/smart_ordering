package com.smartordering.modules.dish.dto;

import lombok.Data;

/**
 * Admin dish paged query params.
 *
 * @author smartordering
 */
@Data
public class DishAdminQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 20;

    private Long categoryId;

    private String name;

    private Integer status;
}