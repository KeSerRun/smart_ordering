package com.smartordering.modules.member.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 会员查询 DTO
 *

 */
@Data
public class MemberQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String memberNo;

    private String nickname;

    private String phone;

    private Long levelId;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
