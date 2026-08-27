package com.smartordering.modules.review.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理端评价列表 VO
 */
@Data
public class AdminReviewListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private String orderNo;
    private String tableCode;
    private Integer overallRating;
    private String content;
    private String customerOpenid;
    private LocalDateTime createTime;
}

