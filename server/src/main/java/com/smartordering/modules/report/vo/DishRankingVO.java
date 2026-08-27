package com.smartordering.modules.report.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Dish ranking view object
 *
 * @author smartordering
 */
@Data
public class DishRankingVO {

    private Long dishId;
    private String dishName;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
}