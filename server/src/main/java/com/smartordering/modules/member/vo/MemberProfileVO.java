package com.smartordering.modules.member.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员档案 VO
 *

 */
@Data
public class MemberProfileVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String memberNo;

    private Long levelId;

    private String levelName;

    private String nickname;

    private String phone;

    private Integer growthValue;

    private Integer pointsBalance;

    private BigDecimal totalAmountConsumed;

    private Integer status;

    private LocalDateTime lastConsumeTime;

    private LocalDateTime createTime;
}
