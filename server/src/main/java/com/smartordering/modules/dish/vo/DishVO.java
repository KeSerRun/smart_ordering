package com.smartordering.modules.dish.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dish view object (returned to app)
 *
 * @author smartordering
 */
@Data
public class DishVO {

    private Long id;
    private Long categoryId;
    private String name;
    private BigDecimal price;
    private String image;
    private String thumbnail;
    private Integer spiceLevel;
    private String specValues;
    private String ingredients;
    private String description;
    private Integer status;
    private Integer soldOut;
    private Integer stock;
    private Integer preparationTime;

    /** Spec items with option prices (populated on app side) */
    private List<DishSpecItemVO> specItems;
}