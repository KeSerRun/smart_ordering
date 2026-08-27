package com.smartordering.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员重置密码 DTO
 */
@Data
public class AdminResetPasswordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String newPassword;
}

