package com.smartordering.modules.feedback.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.feedback.dto.FeedbackCreateDTO;
import com.smartordering.modules.feedback.service.FeedbackService;
import com.smartordering.modules.feedback.vo.FeedbackVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feedback controller (app side)
 *
 * @author smartordering
 */
@Tag(name = "Feedback (App)")
@RestController
@RequestMapping("/app/feedback")
@RequiredArgsConstructor
public class AppFeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "Submit feedback")
    @PostMapping
    public ApiResponse<FeedbackVO> submitFeedback(@Valid @RequestBody FeedbackCreateDTO dto) {
        return ApiResponse.ok(feedbackService.submitFeedback(StpUtil.getLoginIdAsLong(), dto));
    }

    @Operation(summary = "List my feedback")
    @GetMapping("/my")
    public ApiResponse<List<FeedbackVO>> listMyFeedback() {
        return ApiResponse.ok(feedbackService.listMyFeedback(StpUtil.getLoginIdAsLong()));
    }
}