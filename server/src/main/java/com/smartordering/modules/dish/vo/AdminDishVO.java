package com.smartordering.modules.dish.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dish view object for the admin side, including category name and spec items.
 *
 * @author smartordering
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminDishVO {

    private Long id;

    private Long categoryId;

    private String categoryName;

    private String name;

    private BigDecimal price;

    private String image;

    private String thumbnail;

    private Integer spiceLevel;

    private String ingredients;

    private String description;

    private Integer status;

    private Integer soldOut;

    private Integer stock;

    private Integer preparationTime;

    private LocalDateTime createTime;

    /** Spec items deserialized from the entity's spec_values JSON */
    private List<DishSpecItemVO> specItems;
}