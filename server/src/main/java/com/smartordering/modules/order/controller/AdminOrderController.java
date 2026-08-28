package com.smartordering.modules.order.controller;

import com.smartordering.common.result.PageResult;
import com.smartordering.common.result.Result;
import com.smartordering.modules.order.dto.AdminOrderCreateDTO;
import com.smartordering.modules.order.dto.OrderQueryDTO;
import com.smartordering.modules.order.service.OrderService;
import com.smartordering.modules.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Cancel order (pending only, releases table)")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancelAdminOrder(id);
        return Result.success();
    }

    // The app/table-board frontend relies on this admin order action.
    @Operation(summary = "Create order (table ordering, dine-in kickoff)")
    @PostMapping
    public Result<OrderVO> createOrder(@Valid @RequestBody AdminOrderCreateDTO dto) {
        return Result.success(orderService.createAdminOrder(dto));
    }
}