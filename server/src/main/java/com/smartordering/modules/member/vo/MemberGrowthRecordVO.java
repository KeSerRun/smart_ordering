package com.smartordering.modules.member.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MemberGrowthRecordVO {
    private Long id;
    private Integer changeAmount;
    private Integer growthAfter;
    private String remark;
    private LocalDateTime createTime;
}