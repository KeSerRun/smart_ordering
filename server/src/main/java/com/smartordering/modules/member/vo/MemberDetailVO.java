package com.smartordering.modules.member.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;

/**
 * 会员详情 VO
 *

 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberDetailVO extends MemberProfileVO {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer totalPointsEarned;

    private Integer totalPointsUsed;

    private BigDecimal currentLevelPointsRate;

    private BigDecimal currentLevelDiscountRate;

    private String currentLevelBenefitConfig;

    private List<MemberPointsRecordVO> recentPointsRecords;

    private List<MemberGrowthRecordVO> recentGrowthRecords;

    private List<MemberLevelChangeLogVO> recentLevelChangeLogs;

    private List<MemberOrderContributionVO> recentOrderContributions;
}
