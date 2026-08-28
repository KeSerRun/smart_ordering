package com.smartordering.modules.dish.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Spec option view object.
 *
 * @author smartordering
 */
@Data
public class DishSpecOptionVO {

    private Long id;

    private String name;

    private Integer sort;

    /** Price delta when selected (positive extra charge / negative discount / zero none) */
    private BigDecimal price;
}