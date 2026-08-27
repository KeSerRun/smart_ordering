package com.smartordering.modules.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Cash payment DTO
 *
 * @author smartordering
 */
@Data
public class CashPayDTO {

    @NotNull(message = "Order id cannot be null")
    private Long orderId;

    @NotNull(message = "Received amount cannot be null")
    private BigDecimal receivedAmount;
}