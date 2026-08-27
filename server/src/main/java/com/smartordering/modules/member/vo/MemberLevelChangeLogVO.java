package com.smartordering.modules.member.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 会员等级变更日志 VO
 *

 */
@Data
public class MemberLevelChangeLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long memberId;

    private Long userId;

    private Long oldLevelId;

    private String oldLevelName;

    private Long newLevelId;

    private String newLevelName;

    private String changeReason;

    private String bizType;

    private Long bizId;

    private String remark;

    private String createTime;
}
