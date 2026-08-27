package com.smartordering.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 字典数据实体
 *

 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典类型ID
     */
    private Long typeId;

    /**
     * 字典标签
     */
    private String label;

    /**
     * 字典值
     */
    private String value;

    /**
     * 排序
     */
    private Integer orderNum;

    /**
     * 状态（0禁用 1启用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
