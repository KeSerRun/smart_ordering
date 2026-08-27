package com.smartordering.modules.system.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志 VO
 *

 */
@Data
public class OperationLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 模块名称
     */
    private String module;

    /**
     * 操作描述
     */
    private String operation;

    /**
     * 方法名
     */
    private String method;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应结果
     */
    private String responseResult;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 执行时长（毫秒）
     */
    private Long duration;

    /**
     * 状态（0-失败 1-成功）
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
