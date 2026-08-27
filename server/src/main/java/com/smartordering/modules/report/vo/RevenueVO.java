package com.smartordering.modules.report.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Revenue view object
 *
 * @author smartordering
 */
@Data
public class RevenueVO {

    private String date;
    private BigDecimal totalRevenue;
    private Integer orderCount;
}