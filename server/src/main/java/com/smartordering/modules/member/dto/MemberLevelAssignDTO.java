package com.smartordering.modules.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 指定用户设置会员等级 DTO
 *
 * @author smartordering
 */
@Data
public class MemberLevelAssignDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 目标会员等级ID */
    @NotNull(message = "请选择会员等级")
    private Long levelId;

    /** 备注（选填） */
    private String remark;
}