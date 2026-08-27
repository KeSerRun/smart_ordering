package com.smartordering.modules.feedback.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理端反馈列表 VO
 *

 */
@Data
public class AdminFeedbackListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String content;
    private String contactPhone;
    private String customerPhone;
    private String customerNickname;
    private String customerOpenid;
    private Integer status;
    private String replyContent;
    private LocalDateTime replyTime;
    private LocalDateTime createTime;
}
