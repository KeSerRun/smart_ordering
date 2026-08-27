package com.smartordering.modules.member.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberLevelVO {
    private Long id;
    private String levelCode;
    private String levelName;
    private Integer sort;
    private Integer growthThreshold;
    private BigDecimal pointsRate;
    private BigDecimal discountRate;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}