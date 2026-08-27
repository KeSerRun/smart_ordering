package com.smartordering.modules.system.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典数据 VO
 *

 */
@Data
public class DictDataVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典数据ID
     */
    private Long id;

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
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
