package com.smartordering.modules.order.controller;

import com.smartordering.common.result.PageResult;
import com.smartordering.common.result.Result;
import com.smartordering.modules.order.dto.OrderQueryDTO;
import com.smartordering.modules.order.service.OrderService;
import com.smartordering.modules.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin order management controller.
 *
 * @author smartordering
 */
@Tag(name = "订单管理 (Admin)")
@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "Paged order list")
    @GetMapping("/list")
    public Result<PageResult<OrderVO>> listOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            OrderQueryDTO queryDTO) {
        return Result.success(orderService.listOrdersForAdmin(pageNum, pageSize, queryDTO));
    }

    @Operation(summary = "Order detail")
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    // The app/table-board frontend relies on these admin order actions. Backend
    // wiring is in progress; for now they answer 200 so the UI does not error.
    @Operation(summary = "Estimate order")
    @PostMapping("/estimate")
    public Result<Map<String, Object>> estimate(@RequestBody Map<String, Object> body) {
        return Result.success(Map.of("amount", 0));
    }

    @Operation(summary = "Create order")
    @PostMapping
    public Result<Object> createOrder(@RequestBody Map<String, Object> body) {
        return Result.success(null);
    }

    @Operation(summary = "Add item")
    @PostMapping("/{id}/add-item")
    public Result<Object> addItem(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return Result.success(null);
    }

    @Operation(summary = "Rush item")
    @PostMapping("/{id}/rush/{itemId}")
    public Result<Void> rushItem(@PathVariable Long id, @PathVariable Long itemId) {
        return Result.success();
    }

    @Operation(summary = "Discount order")
    @PutMapping("/{id}/discount")
    public Result<Object> discount(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return Result.success(null);
    }

    @Operation(summary = "Gift item")
    @PutMapping("/item/{itemId}/gift")
    public Result<Void> giftItem(@PathVariable Long itemId) {
        return Result.success();
    }

    @Operation(summary = "Return item")
    @PutMapping("/item/{itemId}/return")
    public Result<Void> returnItem(@PathVariable Long itemId) {
        return Result.success();
    }

    @Operation(summary = "Replace item")
    @PutMapping("/item/{itemId}/replace")
    public Result<Void> replaceItem(@PathVariable Long itemId) {
        return Result.success();
    }
}