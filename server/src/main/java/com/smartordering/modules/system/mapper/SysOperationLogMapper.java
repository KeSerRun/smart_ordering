package com.smartordering.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.system.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 *

 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {
}
