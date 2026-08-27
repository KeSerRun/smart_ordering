package com.smartordering.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * Order item mapper
 *
 * @author smartordering
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}