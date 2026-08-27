package com.smartordering.modules.member.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * Member service interface
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_level")
public class MemberLevel extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    private String levelCode;
    private String levelName;
    private Integer sort;
    private Integer growthThreshold;
    private BigDecimal pointsRate;
    private BigDecimal discountRate;
    private String benefitConfig;
    private Long upgradeCouponTemplateId;
    private Long exclusiveCouponTemplateId;
    private Integer status;
    private String remark;
}