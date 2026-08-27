package com.smartordering.modules.dish.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dish create/update payload. {@code id} is present when updating.
 *
 * @author smartordering
 */
@Data
public class DishCreateDTO {

    /** Present when updating */
    private Long id;

    private Long categoryId;

    private String name;

    private BigDecimal price;

    private String image;

    private String thumbnail;

    private Integer spiceLevel;

    private String ingredients;

    private String description;

    private Integer stock;

    private Integer preparationTime;

    /** Spec items, serialized into dish.spec_values (JSON) on save */
    private List<DishSpecItemDTO> specItems;
}