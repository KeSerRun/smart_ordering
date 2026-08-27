package com.smartordering.modules.report.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Table turnover view object
 *
 * @author smartordering
 */
@Data
public class TableTurnoverVO {

    private String date;
    private Integer totalOrders;
    private Integer totalTables;
    private BigDecimal turnoverRate;
}