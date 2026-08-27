package com.smartordering.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 字典类型实体
 *

 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SysDictType extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典名称
     */
    private String name;

    /**
     * 字典编码
     */
    private String code;

    /**
     * 状态（0禁用 1启用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
