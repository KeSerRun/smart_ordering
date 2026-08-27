package com.smartordering.modules.order.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.order.dto.OrderCreateDTO;
import com.smartordering.modules.order.service.OrderService;
import com.smartordering.modules.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Order controller (app side)
 *
 * @author smartordering
 */
@Tag(name = "Order (App)")
@RestController
@RequestMapping("/app/order")
@RequiredArgsConstructor
public class AppOrderController {

    private final OrderService orderService;

    @Operation(summary = "Create order from cart")
    @PostMapping
    public ApiResponse<OrderVO> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        return ApiResponse.ok(orderService.createOrder(getUserId(), dto));
    }

    @Operation(summary = "Get order detail")
    @GetMapping("/{id}")
    public ApiResponse<OrderVO> getOrderDetail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getOrderDetail(id));
    }

    private Long getUserId() {
        return StpUtil.getLoginIdAsLong();
    }
}