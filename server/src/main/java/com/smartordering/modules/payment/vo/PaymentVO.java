package com.smartordering.modules.payment.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment view object
 *
 * @author smartordering
 */
@Data
public class PaymentVO {

    private Long id;
    private Long orderId;
    private String orderNo;
    private String paymentNo;
    private Integer paymentMethod;
    private BigDecimal amount;
    private BigDecimal receivedAmount;
    private BigDecimal changeAmount;
    private Integer status;
    private LocalDateTime createTime;
}