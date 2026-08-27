package com.smartordering.modules.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 会员积分调整 DTO
 *

 */
@Data
public class MemberPointsAdjustDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "调整积分不能为空")
    private Integer changeAmount;

    private String remark;
}
