package com.smartordering.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.order.entity.OrderOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * Order operation log mapper
 *
 * @author smartordering
 */
@Mapper
public interface OrderOperationLogMapper extends BaseMapper<OrderOperationLog> {
}