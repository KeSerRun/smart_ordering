package com.smartordering.modules.member.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 会员等级分布 VO
 *

 */
@Data
public class MemberLevelDistributionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 等级ID
     */
    private Long levelId;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 会员数量
     */
    private Long memberCount;
}
