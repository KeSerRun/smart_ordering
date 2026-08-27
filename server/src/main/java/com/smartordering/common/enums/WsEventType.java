package com.smartordering.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * WebSocket 事件类型枚举
 *
 * @author smartordering
 */
@Getter
@AllArgsConstructor
public enum WsEventType {

    /** 新订单 - 推送至后厨端和服务端 */
    NEW_ORDER("NEW_ORDER", "新订单"),

    /** 催单 - 推送至后厨端和服务端 */
    RUSH_ORDER("RUSH_ORDER", "催单"),

    /** 单品制作完成 - 推送至顾客端和服务端 */
    ITEM_COMPLETED("ITEM_COMPLETED", "单品制作完成"),

    /** 全部出餐 - 推送至服务端 */
    ALL_COMPLETED("ALL_COMPLETED", "全部出餐"),

    /** 菜品估清 - 推送至顾客端和服务端 */
    SOLD_OUT("SOLD_OUT", "菜品估清"),

    /** 桌台状态变更 - 推送至服务端 */
    TABLE_STATUS("TABLE_STATUS", "桌台状态变更");

    /** 事件编码 */
    private final String code;

    /** 事件描述 */
    private final String description;
}