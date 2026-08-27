package com.smartordering.modules.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartordering.modules.order.entity.Order;
import com.smartordering.modules.order.entity.OrderItem;
import com.smartordering.modules.order.mapper.OrderItemMapper;
import com.smartordering.modules.order.mapper.OrderMapper;
import com.smartordering.modules.report.service.ReportService;
import com.smartordering.modules.report.vo.DishRankingVO;
import com.smartordering.modules.report.vo.RevenueVO;
import com.smartordering.modules.report.vo.TableTurnoverVO;
import com.smartordering.modules.table.mapper.DiningTableMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Report service implementation
 *
 * @author smartordering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final DiningTableMapper diningTableMapper;

    @Override
    public List<RevenueVO> getRevenue(String dimension, LocalDate startDate, LocalDate endDate) {
        List<Order> paidOrders = queryPaidOrders(startDate, endDate);

        Map<String, List<Order>> grouped = paidOrders.stream()
                .collect(Collectors.groupingBy(order -> extractDateKey(order.getCreateTime(), dimension),
                        LinkedHashMap::new, Collectors.toList()));

        List<RevenueVO> result = new ArrayList<>();
        for (Map.Entry<String, List<Order>> entry : grouped.entrySet()) {
            RevenueVO vo = new RevenueVO();
            vo.setDate(entry.getKey());
            vo.setOrderCount(entry.getValue().size());
            vo.setTotalRevenue(entry.getValue().stream()
                    .map(Order::getActualAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            result.add(vo);
        }
        result.sort(Comparator.comparing(RevenueVO::getDate));
        return result;
    }

    @Override
    public List<DishRankingVO> getDishRanking(LocalDate startDate, LocalDate endDate, Integer limit) {
        List<Order> paidOrders = queryPaidOrders(startDate, endDate);
        if (paidOrders.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> orderIds = paidOrders.stream().map(Order::getId).toList();

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getOrderId, orderIds);
        List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);

        Map<Long, List<OrderItem>> groupedByDish = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getDishId));

        List<DishRankingVO> result = new ArrayList<>();
        for (Map.Entry<Long, List<OrderItem>> entry : groupedByDish.entrySet()) {
            DishRankingVO vo = new DishRankingVO();
            vo.setDishId(entry.getKey());
            vo.setDishName(entry.getValue().get(0).getDishName());
            vo.setTotalQuantity(entry.getValue().stream().mapToInt(OrderItem::getQuantity).sum());
            vo.setTotalAmount(entry.getValue().stream()
                    .map(OrderItem::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            result.add(vo);
        }
        result.sort(Comparator.comparingInt(DishRankingVO::getTotalQuantity).reversed());

        if (limit != null && limit > 0 && result.size() > limit) {
            return result.subList(0, limit);
        }
        return result;
    }

    @Override
    public List<TableTurnoverVO> getTableTurnover(LocalDate startDate, LocalDate endDate) {
        List<Order> paidOrders = queryPaidOrders(startDate, endDate);

        Long totalTables = diningTableMapper.selectCount(new LambdaQueryWrapper<>());
        int tableCount = totalTables.intValue();
        if (tableCount == 0) {
            tableCount = 1;
        }

        Map<String, List<Order>> grouped = paidOrders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreateTime().toLocalDate().toString(),
                        LinkedHashMap::new, Collectors.toList()));

        List<TableTurnoverVO> result = new ArrayList<>();
        for (Map.Entry<String, List<Order>> entry : grouped.entrySet()) {
            TableTurnoverVO vo = new TableTurnoverVO();
            vo.setDate(entry.getKey());
            vo.setTotalOrders(entry.getValue().size());
            vo.setTotalTables(tableCount);
            vo.setTurnoverRate(BigDecimal.valueOf(entry.getValue().size())
                    .divide(BigDecimal.valueOf(tableCount), 2, RoundingMode.HALF_UP));
            result.add(vo);
        }
        result.sort(Comparator.comparing(TableTurnoverVO::getDate));
        return result;
    }

    // ========== private ==========

    private List<Order> queryPaidOrders(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, 1);
        wrapper.ge(Order::getCreateTime, LocalDateTime.of(startDate, LocalTime.MIN));
        wrapper.le(Order::getCreateTime, LocalDateTime.of(endDate, LocalTime.MAX));
        return orderMapper.selectList(wrapper);
    }

    private String extractDateKey(LocalDateTime dateTime, String dimension) {
        return switch (dimension) {
            case "week" -> {
                int year = dateTime.getYear();
                int week = dateTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                yield year + "-W" + String.format("%02d", week);
            }
            case "month" -> dateTime.getYear() + "-" + String.format("%02d", dateTime.getMonthValue());
            default -> dateTime.toLocalDate().toString();
        };
    }
}