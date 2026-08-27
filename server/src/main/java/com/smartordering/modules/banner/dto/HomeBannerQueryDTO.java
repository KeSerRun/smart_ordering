package com.smartordering.modules.banner.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 首页轮播图查询 DTO
 *

 */
@Data
public class HomeBannerQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标题关键词
     */
    private String title;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 投放位置
     */
    private String scene;

    /**
     * 页码
     */
    private Long pageNum = 1L;

    /**
     * 每页数量
     */
    private Long pageSize = 10L;
}
