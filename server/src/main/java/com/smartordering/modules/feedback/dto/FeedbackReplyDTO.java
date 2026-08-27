package com.smartordering.modules.feedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 反馈回复 DTO
 *

 */
@Data
@Schema(description = "反馈回复参数")
public class FeedbackReplyDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "回复内容")
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容不能超过500字")
    private String replyContent;
}
