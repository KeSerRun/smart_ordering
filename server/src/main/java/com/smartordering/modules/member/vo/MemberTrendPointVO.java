package com.smartordering.modules.member.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 会员趋势点 VO
 *

 */
@Data
public class MemberTrendPointVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    private String date;

    /**
     * 新增会员数
     */
    private Long newMemberCount;
}
