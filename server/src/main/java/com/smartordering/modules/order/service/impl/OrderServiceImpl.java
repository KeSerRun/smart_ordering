package com.smartordering.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.common.result.PageResult;
import com.smartordering.modules.cart.service.CartService;
import com.smartordering.modules.cart.vo.CartItemVO;
import com.smartordering.modules.cart.vo.CartVO;
import com.smartordering.modules.mq.service.ReliableMessageService;
import com.smartordering.modules.order.dto.OrderCreateDTO;
import com.smartordering.modules.order.dto.OrderCreatedEvent;
import com.smartordering.modules.order.dto.OrderQueryDTO;
import com.smartordering.modules.order.entity.Order;
import com.smartordering.modules.order.entity.OrderItem;
import com.smartordering.modules.order.mapper.OrderItemMapper;
import com.smartordering.modules.order.mapper.OrderMapper;
import com.smartordering.modules.order.service.OrderService;
import com.smartordering.modules.order.vo.OrderItemVO;
import com.smartordering.modules.order.vo.OrderVO;
import com.smartordering.modules.table.entity.DiningTable;
import com.smartordering.modules.table.mapper.DiningTableMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Order service implementation
 *
 * @author smartordering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartService cartService;
    private final DiningTableMapper diningTableMapper;
    private final ReliableMessageService reliableMessageService;

    @Override
    @Transactional
    public OrderVO createOrder(Long userId, OrderCreateDTO dto) {
        // 1. Get cart
        CartVO cart = cartService.getCart(userId, dto.getTableId());
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        // 2. Validate table
        DiningTable table = diningTableMapper.selectById(dto.getTableId());
        if (table == null) {
            throw new BusinessException("Table not found");
        }

        // 3. Calculate amount
        BigDecimal originalAmount = cart.getTotalPrice();

        // 4. Insert order
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setTableId(dto.getTableId());
        order.setTableCode(table.getCode());
        order.setOriginalAmount(originalAmount);
        order.setDiscountRate(BigDecimal.ONE);
        order.setActualAmount(originalAmount);
        order.setPointsUsed(0);
        order.setPointsDiscountAmount(BigDecimal.ZERO);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setStatus(0);
        order.setPaymentMode(dto.getPaymentMode() != null ? dto.getPaymentMode() : 1);
        order.setOrderType(dto.getOrderType() != null ? dto.getOrderType() : 0);
        order.setRemark(dto.getRemark());
        orderMapper.insert(order);

        // 5. Insert order items
        for (CartItemVO item : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setDishId(item.getDishId());
            oi.setDishName(item.getDishName());
            oi.setDishImage(item.getDishImage());
            oi.setPrice(item.getPrice());
            oi.setQuantity(item.getQuantity());
            oi.setAmount(item.getAmount());
            oi.setRemark(item.getRemark());
            oi.setStatus(0);
            oi.setPaymentStatus(0);
            oi.setIsGift(0);
            oi.setAddedAt(LocalDateTime.now());
            orderItemMapper.insert(oi);
        }

        // 6. Write reliable message (transactional outbox): publish after commit -> kitchen push
        reliableMessageService.send(
                "ORDER:" + order.getOrderNo(),
                "order.created",
                "NEW_ORDER",
                "ORDER",
                String.valueOf(order.getId()),
                buildOrderCreatedEvent(order, userId, cart));

        // 7. Clear cart
        cartService.clearCart(userId, dto.getTableId());

        log.info("Order created: orderNo={}, tableId={}, amount={}", order.getOrderNo(), dto.getTableId(), originalAmount);
        return getOrderDetail(order.getId());
    }

    /**
     * Build the order-created event payload (written into the reliable-message outbox,
     * published to RabbitMQ, then broadcast to the kitchen screen).
     */
    private OrderCreatedEvent buildOrderCreatedEvent(Order order, Long userId, CartVO cart) {
        List<OrderCreatedEvent.Item> items = cart.getItems().stream()
                .map(i -> OrderCreatedEvent.Item.builder()
                        .dishId(i.getDishId())
                        .dishName(i.getDishName())
                        .quantity(i.getQuantity())
                        .amount(i.getAmount())
                        .remark(i.getRemark())
                        .build())
                .collect(Collectors.toList());
        return OrderCreatedEvent.builder()
                .messageKey("ORDER:" + order.getOrderNo())
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .tableId(order.getTableId())
                .tableCode(order.getTableCode())
                .orderType(order.getOrderType())
                .paymentMode(order.getPaymentMode())
                .status(order.getStatus())
                .originalAmount(order.getOriginalAmount())
                .actualAmount(order.getActualAmount())
                .remark(order.getRemark())
                .userId(userId)
                .createdAt(order.getCreateTime() != null ? order.getCreateTime() : LocalDateTime.now())
                .items(items)
                .build();
    }

    @Override
    public OrderVO getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("Order not found");
        }

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);

        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItemVO> items = orderItemMapper.selectList(wrapper).stream().map(i -> {
            OrderItemVO iv = new OrderItemVO();
            BeanUtils.copyProperties(i, iv);
            return iv;
        }).collect(Collectors.toList());
        vo.setItems(items);
        return vo;
    }

    private String generateOrderNo() {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            int random = ThreadLocalRandom.current().nextInt(100, 999);
            return "SO" + timestamp + random;
        }

        @Override
        public PageResult<OrderVO> listOrdersForAdmin(int pageNum, int pageSize, OrderQueryDTO dto) {
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
                    if (dto != null) {
                        wrapper.eq(dto.getStatus() != null, Order::getStatus, dto.getStatus())
                                .eq(dto.getTableId() != null, Order::getTableId, dto.getTableId())
                                .like(StringUtils.hasText(dto.getOrderNo()), Order::getOrderNo, dto.getOrderNo());
                        if (dto.getStartDate() != null) {
                            wrapper.ge(Order::getCreateTime, dto.getStartDate().atStartOfDay());
                        }
                        if (dto.getEndDate() != null) {
                            wrapper.le(Order::getCreateTime, dto.getEndDate().atStartOfDay().plusDays(1));
                        }
                    }
            wrapper.orderByDesc(Order::getCreateTime);
            Page<Order> page = new Page<>(pageNum, pageSize);
            orderMapper.selectPage(page, wrapper);

            Set<Long> orderIds = page.getRecords().stream().map(Order::getId).collect(Collectors.toSet());
            Map<Long, List<OrderItem>> itemsByOrder = orderIds.isEmpty() ? Collections.emptyMap()
                    : orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds))
                            .stream().collect(Collectors.groupingBy(OrderItem::getOrderId));

            List<OrderVO> list = page.getRecords().stream().map(order -> {
                OrderVO vo = new OrderVO();
                BeanUtils.copyProperties(order, vo);
                vo.setItems(itemsByOrder.getOrDefault(order.getId(), Collections.emptyList()).stream().map(i -> {
                    OrderItemVO iv = new OrderItemVO();
                    BeanUtils.copyProperties(i, iv);
                    return iv;
                }).collect(Collectors.toList()));
                return vo;
            }).collect(Collectors.toList());
            return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
        }
    }