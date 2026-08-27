package com.smartordering.modules.payment.service;

import com.smartordering.common.result.PageResult;
import com.smartordering.modules.payment.dto.CashPayDTO;
import com.smartordering.modules.payment.vo.PaymentVO;

/**
 * Payment service interface
 *
 * @author smartordering
 */
public interface PaymentService {

    PaymentVO cashPay(CashPayDTO dto);

    PaymentVO getPaymentStatus(Long paymentId);

    PageResult<PaymentVO> listPaymentsForAdmin(int pageNum, int pageSize, Integer status, String paymentNo);
}