package com.smartordering.modules.coupon.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发券任务查询 DTO
 *

 */
@Data
public class CouponGrantTaskQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 任务状态
     */
    private Integer taskStatus;

    /**
     * 页码
     */
    private Long pageNum = 1L;

    /**
     * 每页数量
     */
    private Long pageSize = 10L;
}
