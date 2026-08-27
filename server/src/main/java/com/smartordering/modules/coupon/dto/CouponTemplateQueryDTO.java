package com.smartordering.modules.coupon.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 优惠券模板查询 DTO
 *

 */
@Data
public class CouponTemplateQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 页码
     */
    private Long pageNum = 1L;

    /**
     * 每页数量
     */
    private Long pageSize = 10L;
}
