package com.smartordering.modules.system.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Role view object.
 *
 * @author smartordering
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleVO {

    private Long id;
    private String name;
    private String code;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}