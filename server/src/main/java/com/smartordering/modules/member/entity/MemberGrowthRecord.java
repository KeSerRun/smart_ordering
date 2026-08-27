package com.smartordering.modules.member.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Member service interface
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_growth_record")
public class MemberGrowthRecord extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    private Long memberId;
    private Long userId;
    private String bizType;
    private Long bizId;
    private Integer changeAmount;
    private Integer growthAfter;
    private String remark;
}