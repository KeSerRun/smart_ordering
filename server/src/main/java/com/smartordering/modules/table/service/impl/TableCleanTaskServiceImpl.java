package com.smartordering.modules.table.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.modules.system.entity.SysUser;
import com.smartordering.modules.system.mapper.SysUserMapper;
import com.smartordering.modules.table.dto.CleanTaskAssignDTO;
import com.smartordering.modules.table.entity.DiningTable;
import com.smartordering.modules.table.entity.TableCleanTask;
import com.smartordering.modules.table.mapper.DiningTableMapper;
import com.smartordering.modules.table.mapper.TableCleanTaskMapper;
import com.smartordering.modules.table.service.TableCleanTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 桌台清理派发任务实现
 *
 * @author smartordering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TableCleanTaskServiceImpl implements TableCleanTaskService {

    /** 桌台状态：0空闲 1占用 3待清理 */
    private static final Integer TABLE_STATUS_TO_CLEAN = 3;

    private final TableCleanTaskMapper cleanTaskMapper;
    private final DiningTableMapper diningTableMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    @Transactional
    public TableCleanTask assignCleanTask(CleanTaskAssignDTO dto) {
        DiningTable table = diningTableMapper.selectById(dto.getTableId());
        if (table == null) {
            throw new BusinessException("桌台不存在");
        }
        // 占用(1)或待清理(3)均可派发：占用桌台派发后强制转为待清理
        if (table.getStatus() == null || (table.getStatus() != 1 && table.getStatus() != TABLE_STATUS_TO_CLEAN)) {
            throw new BusinessException("仅占用或待清理状态的桌台可以派发");
        }
        // 防止同一桌台重复派发未清理任务
        Long pending = cleanTaskMapper.selectCount(new LambdaQueryWrapper<TableCleanTask>()
                .eq(TableCleanTask::getTableId, dto.getTableId())
                .eq(TableCleanTask::getStatus, 0));
        if (pending != null && pending > 0) {
            throw new BusinessException("该桌台已有未完成的清理任务");
        }

        // 占用桌台强制转为待清理
        if (table.getStatus() == 1) {
            DiningTable update = new DiningTable();
            update.setId(table.getId());
            update.setStatus(TABLE_STATUS_TO_CLEAN);
            diningTableMapper.updateById(update);
            table.setStatus(TABLE_STATUS_TO_CLEAN);
        }

        TableCleanTask task = new TableCleanTask();
        task.setTableId(table.getId());
        task.setTableCode(table.getCode());
        task.setTableName(table.getName());
        if (dto.getAssigneeId() != null) {
            SysUser assignee = sysUserMapper.selectById(dto.getAssigneeId());
            if (assignee == null) {
                throw new BusinessException("清理人不存在");
            }
            task.setAssigneeId(assignee.getId());
            task.setAssigneeName(StringUtils.hasText(assignee.getNickname())
                    ? assignee.getNickname() : assignee.getUsername());
        } else {
            // 未指定清理人：后厨看板按桌台待清理状态自行清理
            task.setAssigneeId(null);
            task.setAssigneeName(null);
        }
        task.setStatus(0); // 待清理
        task.setRemark(dto.getRemark());
        cleanTaskMapper.insert(task);
        log.info("Clean task assigned: table={}, assignee={}, taskId={}",
                table.getCode(), task.getAssigneeName(), task.getId());
        return task;
    }

    @Override
    @Transactional
    public void completeCleanTask(Long taskId) {
        TableCleanTask task = cleanTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("清理任务不存在");
        }
        if (task.getStatus() != null && task.getStatus() == 1) {
            throw new BusinessException("任务已完成，请勿重复操作");
        }
        task.setStatus(1);
        task.setFinishTime(LocalDateTime.now());
        cleanTaskMapper.updateById(task);

        // 联动桌台恢复空闲
        DiningTable table = diningTableMapper.selectById(task.getTableId());
        if (table != null) {
            DiningTable update = new DiningTable();
            update.setId(table.getId());
            update.setStatus(0);
            diningTableMapper.updateById(update);
        }
        log.info("Clean task completed: taskId={}, table={}", taskId, task.getTableCode());
    }

    @Override
    @Transactional
    public void completeCleanByTable(Long tableId) {
        TableCleanTask task = cleanTaskMapper.selectOne(new LambdaQueryWrapper<TableCleanTask>()
                .eq(TableCleanTask::getTableId, tableId)
                .eq(TableCleanTask::getStatus, 0)
                .orderByDesc(TableCleanTask::getCreateTime)
                .last("LIMIT 1"));
        if (task != null) {
            task.setStatus(1);
            task.setFinishTime(LocalDateTime.now());
            cleanTaskMapper.updateById(task);
        }
        DiningTable table = diningTableMapper.selectById(tableId);
        if (table != null) {
            DiningTable update = new DiningTable();
            update.setId(table.getId());
            update.setStatus(0);
            diningTableMapper.updateById(update);
        }
        log.info("Clean by table completed: tableId={}, taskId={}", tableId, task == null ? null : task.getId());
    }

    @Override
    public Page<TableCleanTask> pageCleanTasks(long pageNum, long pageSize, Integer status) {
        LambdaQueryWrapper<TableCleanTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, TableCleanTask::getStatus, status)
                .orderByDesc(TableCleanTask::getCreateTime);
        Page<TableCleanTask> page = new Page<>(pageNum, pageSize);
        cleanTaskMapper.selectPage(page, wrapper);
        return page;
    }
}