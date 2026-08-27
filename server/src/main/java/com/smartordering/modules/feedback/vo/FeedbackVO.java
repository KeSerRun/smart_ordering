package com.smartordering.modules.feedback.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Feedback view object
 *
 * @author smartordering
 */
@Data
public class FeedbackVO {

    private Long id;
    private String content;
    private String replyContent;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime replyTime;
}