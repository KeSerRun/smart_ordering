package com.smartordering.modules.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 评价管理查询 DTO
 */
@Data
@Schema(description = "评价查询参数")
public class ReviewQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "总体评分（1-5）")
    private Integer overallRating;

    @Schema(description = "评价人openid")
    private String customerOpenid;

    @Schema(description = "开始日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}

