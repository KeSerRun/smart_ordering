package com.smartordering.modules.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 管理端编辑用户 DTO（用户名不可改）
 *
 * @author smartordering
 */
@Data
public class AdminUserUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户ID不能为空")
    private Long id;

    private String nickname;

    private String email;

    private String phone;

    private Integer status;

    /** 模块权限编码：core/ops/sys/kitchen */
    private List<String> modules;
}