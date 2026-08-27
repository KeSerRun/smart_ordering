package com.smartordering.modules.coupon.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 发券任务 VO
 *

 */
@Data
public class CouponGrantTaskVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long templateId;
    private String templateName;
    private Integer grantMode;
    private Integer taskStatus;
    private Integer targetCount;
    private Integer successCount;
    private Integer failCount;
    private String remark;
    private LocalDateTime startedTime;
    private String lastError;
    private Integer totalBatchCount;
    private Integer finishedBatchCount;
    private LocalDateTime finishedTime;
}
