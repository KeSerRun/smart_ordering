package com.smartordering.modules.member.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 积分流水查询 DTO
 *

 */
@Data
public class MemberPointsRecordQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long memberId;

    private Long userId;

    private Integer changeType;

    private String bizType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
