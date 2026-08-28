package com.smartordering.modules.table.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 桌台清理派发任务（结账后待清理桌台 → 指派清理人 → 确认已清理）
 *
 * @author smartordering
 */
@Data
@TableName("table_clean_task")
public class TableCleanTask implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 桌台ID */
    private Long tableId;

    /** 桌台代码快照 */
    private String tableCode;

    /** 桌台名称快照 */
    private String tableName;

    /** 清理人ID */
    private Long assigneeId;

    /** 清理人姓名快照 */
    private String assigneeName;

    /** 状态：0待清理 1已清理 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 派发时间 */
    private LocalDateTime createTime;

    /** 完成时间 */
    private LocalDateTime finishTime;

    private Long createBy;

    private Long updateBy;

    private LocalDateTime updateTime;
}