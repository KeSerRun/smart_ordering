package com.smartordering.modules.review.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.review.dto.ReviewCreateDTO;
import com.smartordering.modules.review.service.ReviewService;
import com.smartordering.modules.review.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Review controller (app side)
 *
 * @author smartordering
 */
@Tag(name = "Review (App)")
@RestController
@RequestMapping("/app/review")
@RequiredArgsConstructor
public class AppReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Submit review")
    @PostMapping
    public ApiResponse<ReviewVO> submitReview(@Valid @RequestBody ReviewCreateDTO dto) {
        return ApiResponse.ok(reviewService.submitReview(StpUtil.getLoginIdAsLong(), dto));
    }

    @Operation(summary = "Get order review")
    @GetMapping("/order/{orderId}")
    public ApiResponse<ReviewVO> getOrderReview(@PathVariable Long orderId) {
        return ApiResponse.ok(reviewService.getOrderReview(orderId));
    }
}