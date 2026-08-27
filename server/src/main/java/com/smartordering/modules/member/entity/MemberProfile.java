package com.smartordering.modules.member.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Member service interface
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_profile")
public class MemberProfile extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    private Long userId;
    private String memberNo;
    private Long levelId;
    private Integer growthValue;
    private Integer pointsBalance;
    private Integer totalPointsEarned;
    private Integer totalPointsUsed;
    private BigDecimal totalAmountConsumed;
    private LocalDate birthday;
    private String registerSource;
    private Integer status;
    private LocalDateTime lastConsumeTime;
}