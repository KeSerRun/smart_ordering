package com.smartordering.modules.cart.service;

import com.smartordering.modules.cart.dto.CartItemDTO;
import com.smartordering.modules.cart.vo.CartVO;

/**
 * Cart service interface
 *
 * @author smartordering
 */
public interface CartService {

    CartVO addItem(Long userId, Long tableId, CartItemDTO dto);

    CartVO updateQuantity(Long userId, Long tableId, Long dishId, int quantity);

    CartVO getCart(Long userId, Long tableId);

    void clearCart(Long userId, Long tableId);

    void removeItem(Long userId, Long tableId, Long dishId);
}