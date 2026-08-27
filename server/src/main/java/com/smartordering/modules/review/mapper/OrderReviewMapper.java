package com.smartordering.modules.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.review.entity.OrderReview;
import org.apache.ibatis.annotations.Mapper;

/**
 * Order review mapper
 *
 * @author smartordering
 */
@Mapper
public interface OrderReviewMapper extends BaseMapper<OrderReview> {
}