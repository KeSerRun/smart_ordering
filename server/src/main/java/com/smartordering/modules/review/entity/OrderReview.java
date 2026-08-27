package com.smartordering.modules.review.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Order review entity
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_review")
public class OrderReview extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private Integer overallRating;
    private String content;
    private String customerOpenid;
}