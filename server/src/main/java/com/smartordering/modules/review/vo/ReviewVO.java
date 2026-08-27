package com.smartordering.modules.review.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Review view object
 *
 * @author smartordering
 */
@Data
public class ReviewVO {

    private Long id;
    private Long orderId;
    private Integer overallRating;
    private String content;
    private LocalDateTime createTime;
}