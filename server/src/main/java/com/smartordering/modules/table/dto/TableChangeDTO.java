package com.smartordering.modules.table.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 换桌 DTO
 *

 */
@Data
public class TableChangeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目标桌台ID
     */
    @NotNull(message = "目标桌台ID不能为空")
    private Long targetTableId;
}
