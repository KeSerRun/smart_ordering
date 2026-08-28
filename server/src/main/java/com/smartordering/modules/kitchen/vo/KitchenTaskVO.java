package com.smartordering.modules.kitchen.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后厨任务 VO
 *
 * @author smartordering
 */
@Data
public class KitchenTaskVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private String orderNo;
    private String tableCode;
    private String areaName;
    private Integer paymentMode;
    private Long dishId;
    private String dishName;
    private String dishImage;
    private Integer quantity;
    private String remark;
    /** 状态：0待制作 1制作中 2已完成 */
    private Integer status;
    /** 上菜状态：0未上菜 1已上菜（status=2 已完成时才有意义） */
    private Integer serveStatus;
    private LocalDateTime addedAt;
    private Integer preparationTime;
    private Boolean overtime;
}