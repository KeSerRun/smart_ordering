package com.smartordering.modules.feedback.service;

import com.smartordering.common.result.PageResult;
import com.smartordering.modules.feedback.dto.FeedbackCreateDTO;
import com.smartordering.modules.feedback.dto.FeedbackQueryDTO;
import com.smartordering.modules.feedback.dto.FeedbackReplyDTO;
import com.smartordering.modules.feedback.vo.AdminFeedbackListVO;
import com.smartordering.modules.feedback.vo.FeedbackVO;

import java.util.List;

/**
 * Feedback service interface.
 *
 * @author smartordering
 */
public interface FeedbackService {

    FeedbackVO submitFeedback(Long userId, FeedbackCreateDTO dto);

    List<FeedbackVO> listMyFeedback(Long userId);

    PageResult<AdminFeedbackListVO> listFeedbackForAdmin(int pageNum, int pageSize, FeedbackQueryDTO dto);

    void replyFeedback(Long feedbackId, FeedbackReplyDTO dto);
}