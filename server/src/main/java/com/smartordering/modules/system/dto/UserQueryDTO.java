package com.smartordering.modules.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户查询 DTO
 *

 */
@Data
public class UserQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 是否仅查询会员用户（APP + STRESS）
     */
    private Boolean memberOnly;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 页码
     */
    private Long pageNum = 1L;

    /**
     * 每页数量
     */
    private Long pageSize = 10L;
}
