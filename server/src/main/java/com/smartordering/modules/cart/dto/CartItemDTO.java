package com.smartordering.modules.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Cart item DTO
 *
 * @author smartordering
 */
@Data
public class CartItemDTO {

    @NotNull(message = "Dish id cannot be null")
    private Long dishId;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String remark;
}