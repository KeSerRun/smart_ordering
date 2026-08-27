package com.smartordering.modules.kitchen.service;

import com.smartordering.modules.kitchen.vo.KitchenTaskVO;

import java.util.List;

/**
 * Kitchen service interface
 *
 * @author smartordering
 */
public interface KitchenService {

    List<KitchenTaskVO> getTaskList();

    void acceptTask(Long itemId);

    void completeTask(Long itemId);

    boolean getAutoAcceptEnabled();

    void updateAutoAcceptEnabled(boolean enabled);
}