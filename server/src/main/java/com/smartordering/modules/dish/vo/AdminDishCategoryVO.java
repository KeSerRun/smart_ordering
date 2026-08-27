package com.smartordering.modules.dish.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dish category view object for the admin side, including bound spec groups.
 *
 * @author smartordering
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminDishCategoryVO {

    private Long id;

    private String name;

    private Integer sort;

    private Integer status;

    private String image;

    /** Bound spec group IDs */
    private List<Long> specGroupIds;

    /** Bound spec group names (ordered to match specGroupIds) */
    private List<String> specGroupNames;

    private LocalDateTime createTime;
}