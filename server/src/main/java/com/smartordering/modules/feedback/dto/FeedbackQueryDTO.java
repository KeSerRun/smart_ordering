package com.smartordering.modules.feedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 反馈管理查询 DTO
 *

 */
@Data
@Schema(description = "反馈管理查询参数")
public class FeedbackQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "状态：0待回复，1已回复")
    private Integer status;

    @Schema(description = "关键词（反馈内容）")
    private String keyword;

    @Schema(description = "联系手机号")
    private String contactPhone;

    @Schema(description = "开始日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
