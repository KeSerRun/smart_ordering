package com.smartordering.modules.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order view object
 *
 * @author smartordering
 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long tableId;
    private String tableCode;
    private String tableSessionCode;
    private BigDecimal originalAmount;
    private BigDecimal actualAmount;
    private Integer pointsUsed;
    private BigDecimal pointsDiscountAmount;
    private BigDecimal paidAmount;
    private Integer status;
    private Integer paymentMode;
    private Integer orderType;
    private String remark;
    private String customerOpenid;
    private LocalDateTime createTime;
    private List<OrderItemVO> items;
}