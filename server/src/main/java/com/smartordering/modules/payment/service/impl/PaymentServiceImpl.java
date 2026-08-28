package com.smartordering.modules.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.common.result.PageResult;
import com.smartordering.modules.order.entity.Order;
import com.smartordering.modules.order.mapper.OrderMapper;
import com.smartordering.modules.payment.dto.CashPayDTO;
import com.smartordering.modules.payment.entity.PaymentRecord;
import com.smartordering.modules.payment.mapper.PaymentRecordMapper;
import com.smartordering.modules.payment.service.PaymentService;
import com.smartordering.modules.payment.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Payment service implementation
 *
 * @author smartordering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRecordMapper paymentRecordMapper;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public PaymentVO cashPay(CashPayDTO dto) {
        // 1. Validate order（orderNo 优先，兼容管理端按订单号收银）
        Order order;
        if (StringUtils.hasText(dto.getOrderNo())) {
            order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                    .eq(Order::getOrderNo, dto.getOrderNo()));
        } else {
            order = dto.getOrderId() == null ? null : orderMapper.selectById(dto.getOrderId());
        }
        if (order == null) {
            throw new BusinessException("Order not found");
        }
        if (order.getStatus() == 1) {
            throw new BusinessException("Order already paid");
        }

        // 2. Calculate change
        BigDecimal received = dto.getReceivedAmount();
        BigDecimal actualAmount = order.getActualAmount();
        if (received.compareTo(actualAmount) < 0) {
            throw new BusinessException("Insufficient payment");
        }
        BigDecimal change = received.subtract(actualAmount);

        // 3. Create payment record
        PaymentRecord record = new PaymentRecord();
        record.setOrderId(order.getId());
        record.setPaymentNo(generatePaymentNo());
        record.setPaymentMethod(2);  // Cash
        record.setAmount(actualAmount);
        record.setStatus(1);         // Paid
        record.setPayerOpenid(null);
        paymentRecordMapper.insert(record);

        // 4. Update order status
        order.setStatus(1);
        order.setPaidAmount(actualAmount);
        orderMapper.updateById(order);

        // 5. Build response
        PaymentVO vo = new PaymentVO();
        vo.setId(record.getId());
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPaymentNo(record.getPaymentNo());
        vo.setPaymentMethod(2);
        vo.setAmount(actualAmount);
        vo.setReceivedAmount(received);
        vo.setChangeAmount(change);
        vo.setStatus(1);
        vo.setCreateTime(record.getCreateTime());

        log.info("Cash payment success: orderId={}, orderNo={}, amount={}, change={}",
                order.getId(), order.getOrderNo(), actualAmount, change);
        return vo;
    }

    @Override
    public PaymentVO getPaymentStatus(Long paymentId) {
        PaymentRecord record = paymentRecordMapper.selectById(paymentId);
        if (record == null) {
            throw new BusinessException("Payment record not found");
        }
        PaymentVO vo = new PaymentVO();
        vo.setId(record.getId());
        vo.setOrderId(record.getOrderId());
        vo.setPaymentNo(record.getPaymentNo());
        vo.setPaymentMethod(record.getPaymentMethod());
        vo.setAmount(record.getAmount());
        vo.setStatus(record.getStatus());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }

    private String generatePaymentNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int random = ThreadLocalRandom.current().nextInt(100, 999);
        return "PY" + timestamp + random;
    }

    @Override
    public PageResult<PaymentVO> listPaymentsForAdmin(int pageNum, int pageSize, Integer status, String paymentNo) {
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, PaymentRecord::getStatus, status)
                .like(StringUtils.hasText(paymentNo), PaymentRecord::getPaymentNo, paymentNo)
                .orderByDesc(PaymentRecord::getCreateTime);
        Page<PaymentRecord> page = new Page<>(pageNum, pageSize);
        paymentRecordMapper.selectPage(page, wrapper);
        List<PaymentVO> list = page.getRecords().stream().map(r -> {
            PaymentVO vo = new PaymentVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).collect(Collectors.toList());

        // 联查业务订单号：payment_record 只存 order_id
        Set<Long> orderIds = list.stream()
                .map(PaymentVO::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!orderIds.isEmpty()) {
            Map<Long, String> orderNoMap = orderMapper.selectBatchIds(orderIds).stream()
                    .collect(Collectors.toMap(Order::getId, Order::getOrderNo));
            list.forEach(vo -> {
                if (vo.getOrderId() != null) {
                    vo.setOrderNo(orderNoMap.get(vo.getOrderId()));
                }
            });
        }
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }
}