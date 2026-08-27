package com.smartordering.modules.member.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * Member service interface
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_points_record")
public class MemberPointsRecord extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    private Long memberId;
    private Long userId;
    private Integer changeType;
    private String bizType;
    private Long bizId;
    private Integer changeAmount;
    private Integer balanceAfter;
    private LocalDateTime expireTime;
    private String remark;
}