package com.smartordering.modules.member.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 会员统计总览 VO
 *

 */
@Data
public class MemberOverviewVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会员总数
     */
    private Long totalMembers;

    /**
     * 活跃会员数
     */
    private Long activeMembers;

    /**
     * 冻结会员数
     */
    private Long frozenMembers;

    /**
     * 总积分余额
     */
    private Long totalPointsBalance;

    /**
     * 总成长值
     */
    private Long totalGrowthValue;

    /**
     * 累计消费总额
     */
    private BigDecimal totalAmountConsumed;

    /**
     * 最近7天新增会员数
     */
    private Long recentNewMembers;

    /**
     * 等级分布
     */
    private List<MemberLevelDistributionVO> levelDistribution;

    /**
     * 近7天新增趋势
     */
    private List<MemberTrendPointVO> recentTrend;
}
