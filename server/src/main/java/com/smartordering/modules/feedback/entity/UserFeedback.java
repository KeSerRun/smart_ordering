package com.smartordering.modules.feedback.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * User feedback entity
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_feedback")
public class UserFeedback extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String customerOpenid;
    private String customerPhone;
    private String contactPhone;
    private String content;
    private String replyContent;
    private LocalDateTime replyTime;
    /** Status: 0=pending 1=replied */
    private Integer status;
}