package com.smartordering.modules.system.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端用户列表 VO
 *
 * @author smartordering
 */
@Data
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 微信 openid
     */
    private String openid;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 分配的角色 ID 列表
     */
    private List<Long> roleIds;

    /**
     * 模块权限编码列表（用户所有角色的模块并集）：core/ops/sys/kitchen
     */
    private List<String> modules;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}