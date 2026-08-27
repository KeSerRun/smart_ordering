package com.smartordering.modules.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 会员等级状态 DTO
 *

 */
@Data
public class MemberLevelStatusDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
