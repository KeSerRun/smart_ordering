package com.smartordering.modules.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.io.Serial;
import java.io.Serializable;

/**
 * 现金收银 DTO（orderId / orderNo 二选一）
 *
 * @author smartordering
 */
@Data
public class CashPayDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 订单ID（与 orderNo 二选一） */
    private Long orderId;

    /** 业务订单号（与 orderId 二选一，管理端收银按订单号输入） */
    private String orderNo;

    /** 实收金额 */
    @NotNull(message = "实收金额不能为空")
    private BigDecimal receivedAmount;
}