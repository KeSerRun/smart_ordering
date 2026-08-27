package com.smartordering.modules.feedback.controller;

import com.smartordering.common.result.PageResult;
import com.smartordering.common.result.Result;
import com.smartordering.modules.feedback.dto.FeedbackQueryDTO;
import com.smartordering.modules.feedback.dto.FeedbackReplyDTO;
import com.smartordering.modules.feedback.service.FeedbackService;
import com.smartordering.modules.feedback.vo.AdminFeedbackListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Admin feedback controller.
 *
 * @author smartordering
 */
@Tag(name = "反馈 (Admin)")
@RestController
@RequestMapping("/admin/feedback")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "Paged feedback list")
    @GetMapping("/list")
    public Result<PageResult<AdminFeedbackListVO>> listFeedback(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            FeedbackQueryDTO queryDTO) {
        return Result.success(feedbackService.listFeedbackForAdmin(pageNum, pageSize, queryDTO));
    }

    @Operation(summary = "Reply to feedback")
    @PutMapping("/{feedbackId}/reply")
    public Result<Void> replyFeedback(@PathVariable Long feedbackId, @Valid @RequestBody FeedbackReplyDTO dto) {
        feedbackService.replyFeedback(feedbackId, dto);
        return Result.success();
    }
}