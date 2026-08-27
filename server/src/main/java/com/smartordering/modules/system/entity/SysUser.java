package com.smartordering.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * System user entity
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String openid;
    private String userType;
    private String avatar;
    private Integer status;
}