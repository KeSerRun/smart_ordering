package com.smartordering.modules.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Order operation log entity (audit log source)
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_operation_log")
public class OrderOperationLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private Long orderItemId;
    private String operationType;
    private Long operatorId;
    private String operatorName;
    private String reason;
    private String detail;
}