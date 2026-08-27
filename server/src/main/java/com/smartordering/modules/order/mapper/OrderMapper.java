package com.smartordering.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * Order mapper
 *
 * @author smartordering
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}