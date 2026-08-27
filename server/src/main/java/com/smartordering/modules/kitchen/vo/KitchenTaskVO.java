package com.smartordering.modules.kitchen.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Kitchen task view object
 *
 * @author smartordering
 */
@Data
public class KitchenTaskVO {

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
    /** Status: 0=pending 1=cooking 2=done */
    private Integer status;
    private LocalDateTime addedAt;
    private Integer preparationTime;
    private Boolean overtime;
}