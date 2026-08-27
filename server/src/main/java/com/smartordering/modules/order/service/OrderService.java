package com.smartordering.modules.order.service;

import com.smartordering.common.result.PageResult;
import com.smartordering.modules.order.dto.OrderCreateDTO;
import com.smartordering.modules.order.dto.OrderQueryDTO;
import com.smartordering.modules.order.vo.OrderVO;

/**
 * Order service interface
 *
 * @author smartordering
 */
public interface OrderService {

    OrderVO createOrder(Long userId, OrderCreateDTO dto);

    OrderVO getOrderDetail(Long orderId);

    PageResult<OrderVO> listOrdersForAdmin(int pageNum, int pageSize, OrderQueryDTO dto);
}