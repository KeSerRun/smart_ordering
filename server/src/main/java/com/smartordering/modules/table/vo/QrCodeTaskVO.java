package com.smartordering.modules.table.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 二维码任务状态 VO
 *

 */
@Data
public class QrCodeTaskVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务类型：GENERATE_ALL / DOWNLOAD_ALL
     */
    private String taskType;

    /**
     * 任务状态：PENDING / RUNNING / SUCCESS / FAILED
     */
    private String status;

    /**
     * 任务提示信息
     */
    private String message;

    /**
     * 总数
     */
    private Integer total;

    /**
     * 已完成数量
     */
    private Integer completed;

    /**
     * 是否可下载
     */
    private Boolean downloadable;

    /**
     * 下载文件名
     */
    private String fileName;

    /**
     * 下载文件路径
     */
    private String filePath;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
}
