package com.smartordering.modules.system.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志 VO
 *

 */
@Data
public class LoginLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 登录地点
     */
    private String location;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 状态（0-失败 1-成功）
     */
    private Integer status;

    /**
     * 消息
     */
    private String message;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
}
