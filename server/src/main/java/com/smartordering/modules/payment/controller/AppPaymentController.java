package com.smartordering.modules.payment.controller;

import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.payment.dto.CashPayDTO;
import com.smartordering.modules.payment.service.PaymentService;
import com.smartordering.modules.payment.vo.PaymentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Payment controller (app side)
 *
 * @author smartordering
 */
@Tag(name = "Payment (App)")
@RestController
@RequestMapping("/app/payment")
@RequiredArgsConstructor
public class AppPaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Cash payment")
    @PostMapping("/cash")
    public ApiResponse<PaymentVO> cashPay(@Valid @RequestBody CashPayDTO dto) {
        return ApiResponse.ok(paymentService.cashPay(dto));
    }

    @Operation(summary = "Get payment status")
    @GetMapping("/{id}/status")
    public ApiResponse<PaymentVO> getPaymentStatus(@PathVariable Long id) {
        return ApiResponse.ok(paymentService.getPaymentStatus(id));
    }
}