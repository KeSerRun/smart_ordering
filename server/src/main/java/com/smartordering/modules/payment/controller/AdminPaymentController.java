package com.smartordering.modules.payment.controller;

import com.smartordering.common.result.PageResult;
import com.smartordering.common.result.Result;
import com.smartordering.modules.payment.dto.CashPayDTO;
import com.smartordering.modules.payment.service.PaymentService;
import com.smartordering.modules.payment.vo.PaymentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin payment controller.
 *
 * @author smartordering
 */
@Tag(name = "支付管理 (Admin)")
@RestController
@RequestMapping("/admin/payment")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Paged payment records")
    @GetMapping("/list")
    public Result<PageResult<PaymentVO>> listPayments(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String paymentNo) {
        return Result.success(paymentService.listPaymentsForAdmin(pageNum, pageSize, status, paymentNo));
    }

    @Operation(summary = "Cash payment")
    @PostMapping("/cash")
    public Result<PaymentVO> cashPay(@Valid @RequestBody CashPayDTO dto) {
        return Result.success(paymentService.cashPay(dto));
    }

    // Checkout/table-board pages call these (wiring in progress). Returning 200 keeps the UI usable.
    @Operation(summary = "Generate payment QR")
    @PostMapping("/qrcode")
    public Result<Map<String, Object>> generateQrCode(@RequestParam Long orderId) {
        return Result.success(Map.of("orderId", orderId, "qrCode", ""));
    }

    @Operation(summary = "Split bill")
    @PostMapping("/split-bill")
    public Result<Void> splitBill(@RequestBody Map<String, Object> body) {
        return Result.success();
    }

    @Operation(summary = "Refund order")
    @PostMapping("/order/{orderId}/refund")
    public Result<Void> refundOrder(@PathVariable Long orderId) {
        return Result.success();
    }

    @Operation(summary = "Payment status")
    @GetMapping("/{id}/status")
    public Result<PaymentVO> getPaymentStatus(@PathVariable Long id) {
        return Result.success(paymentService.getPaymentStatus(id));
    }
}