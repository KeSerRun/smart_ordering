package com.smartordering.modules.cart.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Cart item view object
 *
 * @author smartordering
 */
@Data
public class CartItemVO {

    private Long dishId;
    private String dishName;
    private String dishImage;
    private BigDecimal price;
    private Integer quantity;
    private String remark;
    private BigDecimal amount;
}