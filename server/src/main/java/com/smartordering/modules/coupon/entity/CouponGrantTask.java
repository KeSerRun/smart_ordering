package com.smartordering.modules.coupon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 发券任务实体（后台发放优惠券的 MQ 异步任务）
 *
 * @author smartordering
 */
@Data
@TableName("coupon_grant_task")
public class CouponGrantTask implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 模板ID */
    private Long templateId;

    /** 模板名称快照 */
    private String templateName;

    /** 发放方式：1指定用户 2全部用户 3按会员等级 */
    private Integer grantMode;

    /** 目标人数 */
    private Integer targetCount;

    /** 成功发放数 */
    private Integer successCount;

    /** 失败数 */
    private Integer failCount;

    /** 状态：0待处理 1处理中 2成功 3失败 */
    private Integer taskStatus;

    /** 总批次数 */
    private Integer batchCount;

    /** 已完成批次数 */
    private Integer finishedBatch;

    /** 按等级发放时的等级ID（逗号分隔） */
    private String levelIds;

    /** 指定用户ID（逗号分隔，grantMode=1 时） */
    private String userIds;

    /** 备注 */
    private String remark;

    /** 最近错误 */
    private String lastError;

    /** 开始时间 */
    private LocalDateTime startedTime;

    /** 完成时间 */
    private LocalDateTime finishedTime;

    private Long createBy;

    private Long updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}