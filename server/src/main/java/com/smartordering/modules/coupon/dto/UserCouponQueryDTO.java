package com.smartordering.modules.coupon.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户优惠券查询 DTO
 *

 */
@Data
public class UserCouponQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 状态
     */
    private Integer status;

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
