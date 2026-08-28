package com.smartordering.modules.table.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.table.entity.TableCleanTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 桌台清理派发任务 Mapper
 *
 * @author smartordering
 */
@Mapper
public interface TableCleanTaskMapper extends BaseMapper<TableCleanTask> {
}