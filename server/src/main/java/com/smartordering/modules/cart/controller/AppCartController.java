package com.smartordering.modules.cart.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.cart.dto.CartItemDTO;
import com.smartordering.modules.cart.service.CartService;
import com.smartordering.modules.cart.vo.CartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Cart controller (app side)
 *
 * @author smartordering
 */
@Tag(name = "Cart (App)")
@RestController
@RequestMapping("/app/cart")
@RequiredArgsConstructor
public class AppCartController {

    private final CartService cartService;

    @Operation(summary = "Get cart")
    @GetMapping
    public ApiResponse<CartVO> getCart(@RequestParam Long tableId) {
        return ApiResponse.ok(cartService.getCart(getUserId(), tableId));
    }

    @Operation(summary = "Add item to cart")
    @PostMapping("/item")
    public ApiResponse<CartVO> addItem(@RequestParam Long tableId,
                                       @Valid @RequestBody CartItemDTO dto) {
        return ApiResponse.ok(cartService.addItem(getUserId(), tableId, dto));
    }

    @Operation(summary = "Update item quantity")
    @PutMapping("/item/{dishId}")
    public ApiResponse<CartVO> updateQuantity(@PathVariable Long dishId,
                                              @RequestParam Long tableId,
                                              @RequestParam Integer quantity) {
        return ApiResponse.ok(cartService.updateQuantity(getUserId(), tableId, dishId, quantity));
    }

    @Operation(summary = "Remove item from cart")
    @DeleteMapping("/item/{dishId}")
    public ApiResponse<Void> removeItem(@PathVariable Long dishId,
                                        @RequestParam Long tableId) {
        cartService.removeItem(getUserId(), tableId, dishId);
        return ApiResponse.ok();
    }

    @Operation(summary = "Clear cart")
    @DeleteMapping
    public ApiResponse<Void> clearCart(@RequestParam Long tableId) {
        cartService.clearCart(getUserId(), tableId);
        return ApiResponse.ok();
    }

    private Long getUserId() {
        return StpUtil.getLoginIdAsLong();
    }
}