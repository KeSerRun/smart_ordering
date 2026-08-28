package com.smartordering.modules.kitchen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.modules.dish.entity.Dish;
import com.smartordering.modules.dish.mapper.DishMapper;
import com.smartordering.modules.kitchen.service.KitchenService;
import com.smartordering.modules.kitchen.vo.KitchenTaskVO;
import com.smartordering.modules.order.entity.Order;
import com.smartordering.modules.order.entity.OrderItem;
import com.smartordering.modules.order.mapper.OrderItemMapper;
import com.smartordering.modules.order.mapper.OrderMapper;
import com.smartordering.modules.table.entity.DiningTable;
import com.smartordering.modules.table.mapper.DiningTableMapper;
import com.smartordering.modules.system.entity.SysConfig;
import com.smartordering.modules.system.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Kitchen service implementation.
 *
 * @author smartordering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KitchenServiceImpl implements KitchenService {

    private static final String AUTO_ACCEPT_KEY = "kitchen.autoAccept";

    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final DiningTableMapper diningTableMapper;
    private final DishMapper dishMapper;
    private final SysConfigMapper sysConfigMapper;

    @Override
    public List<KitchenTaskVO> getTaskList() {
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getStatus, 0, 1).orderByAsc(OrderItem::getAddedAt);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> orderIds = items.stream().map(OrderItem::getOrderId).collect(Collectors.toSet());
        Map<Long, Order> orderMap = orderMapper.selectBatchIds(orderIds).stream()
                .collect(Collectors.toMap(Order::getId, o -> o));

        Set<Long> tableIds = orderMap.values().stream().map(Order::getTableId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, DiningTable> tableMap = tableIds.isEmpty() ? Collections.emptyMap()
                : diningTableMapper.selectBatchIds(tableIds).stream()
                        .collect(Collectors.toMap(DiningTable::getId, t -> t));

        Set<Long> dishIds = items.stream().map(OrderItem::getDishId).collect(Collectors.toSet());
        Map<Long, Dish> dishMap = dishIds.isEmpty() ? Collections.emptyMap()
                : dishMapper.selectBatchIds(dishIds).stream()
                        .collect(Collectors.toMap(Dish::getId, d -> d));

        LocalDateTime now = LocalDateTime.now();
        List<KitchenTaskVO> result = new ArrayList<>();
        for (OrderItem item : items) {
            Order order = orderMap.get(item.getOrderId());
            if (order == null) {
                continue;
            }
            KitchenTaskVO vo = new KitchenTaskVO();
            vo.setId(item.getId());
            vo.setOrderId(item.getOrderId());
            vo.setOrderNo(order.getOrderNo());
            vo.setTableCode(order.getTableCode());
            DiningTable table = tableMap.get(order.getTableId());
            vo.setAreaName(table == null ? null : table.getAreaName());
            vo.setPaymentMode(order.getPaymentMode());
            vo.setDishId(item.getDishId());
            vo.setDishName(item.getDishName());
            vo.setDishImage(item.getDishImage());
            vo.setQuantity(item.getQuantity());
            vo.setRemark(item.getRemark());
            vo.setStatus(item.getStatus());
            vo.setAddedAt(item.getAddedAt());

            Dish dish = dishMap.get(item.getDishId());
            if (dish != null && dish.getPreparationTime() != null) {
                vo.setPreparationTime(dish.getPreparationTime());
                vo.setOvertime(item.getAddedAt() != null
                        && now.isAfter(item.getAddedAt().plusMinutes(dish.getPreparationTime())));
            } else {
                vo.setPreparationTime(null);
                vo.setOvertime(false);
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional
    public void acceptTask(Long itemId) {
        OrderItem item = orderItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("Order item not found");
        }
        if (item.getStatus() != 0) {
            throw new BusinessException("Item is not in pending status");
        }
        item.setStatus(1);
        orderItemMapper.updateById(item);
        log.info("Kitchen accept task, itemId={}", itemId);
    }

    @Override
    @Transactional
    public void completeTask(Long itemId) {
        OrderItem item = orderItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("Order item not found");
        }
        if (item.getStatus() != 1) {
            throw new BusinessException("Item is not in cooking status");
        }
        item.setStatus(2);
        orderItemMapper.updateById(item);
        log.info("Kitchen complete task, itemId={}", itemId);
    }

    @Override
    @Transactional
    public int autoAcceptByOrder(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
                        .eq(OrderItem::getStatus, 0));
        for (OrderItem item : items) {
            item.setStatus(1);
            orderItemMapper.updateById(item);
        }
        if (!items.isEmpty()) {
            log.info("Kitchen auto-accept: orderId={}, accepted={}", orderId, items.size());
        }
        return items.size();
    }

    @Override
    public boolean getAutoAcceptEnabled() {
        SysConfig config = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, AUTO_ACCEPT_KEY));
        return config != null && "true".equalsIgnoreCase(config.getConfigValue());
    }

    @Override
    public void updateAutoAcceptEnabled(boolean enabled) {
        SysConfig existing = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, AUTO_ACCEPT_KEY));
        if (existing == null) {
            SysConfig config = new SysConfig();
            config.setName("Kitchen auto-accept");
            config.setConfigKey(AUTO_ACCEPT_KEY);
            config.setConfigValue(Boolean.toString(enabled));
            sysConfigMapper.insert(config);
        } else {
            SysConfig update = new SysConfig();
            update.setId(existing.getId());
            update.setConfigValue(Boolean.toString(enabled));
            sysConfigMapper.updateById(update);
        }
    }
}