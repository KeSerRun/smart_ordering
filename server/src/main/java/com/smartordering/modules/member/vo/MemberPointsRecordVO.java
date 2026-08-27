package com.smartordering.modules.member.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MemberPointsRecordVO {
    private Long id;
    private Integer changeAmount;
    private Integer balanceAfter;
    private String remark;
    private LocalDateTime createTime;
}