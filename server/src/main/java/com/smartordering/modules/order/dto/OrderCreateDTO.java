package com.smartordering.modules.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Order create DTO
 *
 * @author smartordering
 */
@Data
public class OrderCreateDTO {

    @NotNull(message = "Table id cannot be null")
    private Long tableId;

    /** Payment mode: 0=pay first 1=pay after (default) */
    private Integer paymentMode = 1;

    /** Order type: 0=dine-in (default) 1=takeaway */
    private Integer orderType = 0;

    private String remark;
}