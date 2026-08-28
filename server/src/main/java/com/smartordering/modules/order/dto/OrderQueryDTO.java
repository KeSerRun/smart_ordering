package com.smartordering.modules.order.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 订单查询 DTO（管理端筛选条件）
 *

 */
@Data
public class OrderQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 订单状态（0待支付 1已支付 2已取消）
     */
    private Integer status;

    /**
         * 桌台ID
         */
        private Long tableId;

        /**
         * 桌台号（桌台代码，模糊搜索，收银按桌台号查单用）
         */
        private String tableCode;

        /**
         * 订单编号（模糊搜索）
         */
        private String orderNo;

    /**
     * 区域名称
     */
    private String areaName;
}
