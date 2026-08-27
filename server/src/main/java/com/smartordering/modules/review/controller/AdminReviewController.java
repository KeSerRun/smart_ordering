package com.smartordering.modules.review.controller;

import com.smartordering.common.result.PageResult;
import com.smartordering.common.result.Result;
import com.smartordering.modules.review.dto.ReviewQueryDTO;
import com.smartordering.modules.review.service.ReviewService;
import com.smartordering.modules.review.vo.AdminReviewListVO;
import com.smartordering.modules.review.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Admin review management controller.
 *
 * @author smartordering
 */
@Tag(name = "评价管理 (Admin)")
@RestController
@RequestMapping("/admin/review")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Paged review list")
    @GetMapping("/list")
    public Result<PageResult<AdminReviewListVO>> listReviews(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            ReviewQueryDTO dto) {
        return Result.success(reviewService.listReviews(pageNum, pageSize, dto));
    }

    @Operation(summary = "Order review detail")
    @GetMapping("/order/{orderId}")
    public Result<ReviewVO> getOrderReview(@PathVariable Long orderId) {
        return Result.success(reviewService.getOrderReview(orderId));
    }
}