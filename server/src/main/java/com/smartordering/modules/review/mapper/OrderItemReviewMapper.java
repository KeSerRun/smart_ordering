package com.smartordering.modules.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.review.entity.OrderItemReview;
import org.apache.ibatis.annotations.Mapper;

/**
 * Order item review mapper
 *
 * @author smartordering
 */
@Mapper
public interface OrderItemReviewMapper extends BaseMapper<OrderItemReview> {
}