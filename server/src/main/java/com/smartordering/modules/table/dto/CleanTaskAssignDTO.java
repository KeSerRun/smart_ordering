package com.smartordering.modules.table.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 桌台清理派发 DTO
 *
 * @author smartordering
 */
@Data
public class CleanTaskAssignDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 桌台ID */
    @NotNull(message = "桌台不能为空")
    private Long tableId;

    /** 清理人ID（后台用户，可空：不指定时后厨自行清理） */
    private Long assigneeId;

    /** 备注 */
    private String remark;
}