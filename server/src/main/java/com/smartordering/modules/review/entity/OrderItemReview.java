package com.smartordering.modules.review.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Order item review entity
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_item_review")
public class OrderItemReview extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long reviewId;
    private Long orderItemId;
    private Integer rating;
}