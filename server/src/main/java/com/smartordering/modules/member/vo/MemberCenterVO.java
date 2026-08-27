package com.smartordering.modules.member.vo;

import lombok.Data;

@Data
public class MemberCenterVO {
    private Long memberId;
    private String memberNo;
    private String levelName;
    private Integer growthValue;
    private Integer nextLevelGrowth;
    private Integer pointsBalance;
    private Integer totalPointsEarned;
}