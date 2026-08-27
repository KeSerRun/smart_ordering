package com.smartordering.modules.review.service;

import com.smartordering.common.result.PageResult;
import com.smartordering.modules.review.dto.ReviewCreateDTO;
import com.smartordering.modules.review.dto.ReviewQueryDTO;
import com.smartordering.modules.review.vo.AdminReviewListVO;
import com.smartordering.modules.review.vo.ReviewVO;

/**
 * Review service interface
 *
 * @author smartordering
 */
public interface ReviewService {

    ReviewVO submitReview(Long userId, ReviewCreateDTO dto);

    ReviewVO getOrderReview(Long orderId);

    PageResult<AdminReviewListVO> listReviews(int pageNum, int pageSize, ReviewQueryDTO dto);
}