package com.smartordering.modules.member.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员订单贡献 VO
 *

 */
@Data
public class MemberOrderContributionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId;

    private String orderNo;

    private BigDecimal actualAmount;

    private BigDecimal paidAmount;

    private Integer pointsReward;

    private Integer growthReward;

    private String createTime;
}
