package com.smartordering.modules.kitchen.service;

import com.smartordering.modules.kitchen.vo.KitchenTaskVO;

import java.util.List;

/**
 * Kitchen service interface.
 *
 * @author smartordering
 */
public interface KitchenService {

    List<KitchenTaskVO> getTaskList();

    void acceptTask(Long itemId);

    void completeTask(Long itemId);

    /** 自动接单：把某订单下所有待接(0)菜品任务一次性置为制作中(1)，返回接单数 */
    int autoAcceptByOrder(Long orderId);

    /** 上菜：已完成(status=2)且未上菜的任务标记为已上菜 */
    void serveItem(Long itemId);

    boolean getAutoAcceptEnabled();

    void updateAutoAcceptEnabled(boolean enabled);
}