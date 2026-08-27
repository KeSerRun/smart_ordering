package com.smartordering.modules.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cart view object
 *
 * @author smartordering
 */
@Data
public class CartVO {

    private Long tableId;
    private List<CartItemVO> items;
    private Integer totalCount;
    private BigDecimal totalPrice;
}