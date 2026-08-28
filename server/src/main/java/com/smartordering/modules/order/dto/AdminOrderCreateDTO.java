package com.smartordering.modules.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 管理端点餐 DTO（桌台开台点餐：直接传菜品明细创建订单，不走购物车）
 *
 * @author smartordering
 */
@Data
public class AdminOrderCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 桌台ID */
    @NotNull(message = "桌台不能为空")
    private Long tableId;

    /** 菜品明细 */
    @Valid
    @NotEmpty(message = "请至少选择一个菜品")
    private List<Item> items;

    /** 备注 */
    private String remark;

    @Data
    public static class Item implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /** 菜品ID */
        @NotNull(message = "菜品不能为空")
        private Long dishId;

        /** 数量 */
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为1")
        private Integer quantity;
    }
}