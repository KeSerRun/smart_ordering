package com.smartordering.modules.order.service;

import com.smartordering.common.result.PageResult;
import com.smartordering.modules.order.dto.AdminOrderCreateDTO;
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

    /** 管理端点餐：桌台开台直接下单（不走购物车） */
    OrderVO createAdminOrder(AdminOrderCreateDTO dto);

    OrderVO getOrderDetail(Long orderId);

    PageResult<OrderVO> listOrdersForAdmin(int pageNum, int pageSize, OrderQueryDTO dto);
}