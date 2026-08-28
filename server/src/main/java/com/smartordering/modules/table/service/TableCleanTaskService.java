package com.smartordering.modules.table.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.modules.table.dto.CleanTaskAssignDTO;
import com.smartordering.modules.table.entity.TableCleanTask;

/**
 * 桌台清理派发 Service
 *
 * @author smartordering
 */
public interface TableCleanTaskService {

    /** 派发清理任务：桌台必须为待清理(3)状态，记录清理人 */
    TableCleanTask assignCleanTask(CleanTaskAssignDTO dto);

    /** 完成清理：任务置已清理 + 桌台恢复空闲 */
    void completeCleanTask(Long taskId);

    /** 按桌台完成清理：有未完成任务一并置完成，无任务则直接恢复桌台空闲 */
    void completeCleanByTable(Long tableId);

    /** 清理任务分页（可过滤状态） */
    Page<TableCleanTask> pageCleanTasks(long pageNum, long pageSize, Integer status);
}