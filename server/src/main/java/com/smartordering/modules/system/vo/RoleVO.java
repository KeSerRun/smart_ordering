package com.smartordering.modules.system.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色 VO
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

    /** 模块权限编码列表：core/ops/sys/kitchen */
    private List<String> modules;

    private LocalDateTime createTime;
}
