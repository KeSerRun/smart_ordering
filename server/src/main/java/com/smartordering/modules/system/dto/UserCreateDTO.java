package com.smartordering.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 管理端新建用户 DTO
 *
 * @author smartordering
 */
@Data
public class UserCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String nickname;

    private String email;

    private String phone;

    private String userType;

    private Integer status;

    /** 分配的角色 ID 列表（角色管理里给角色配置模块权限） */
    private List<Long> roleIds;
}