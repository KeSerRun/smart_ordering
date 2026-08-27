package com.smartordering.modules.coupon.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发券任务明细查询 DTO
 *

 */
@Data
public class CouponGrantTaskDetailQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 用户状态
     */
    private Integer grantStatus;

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 页码
     */
    private Long pageNum = 1L;

    /**
     * 每页数量
     */
    private Long pageSize = 10L;
}
