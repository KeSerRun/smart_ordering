package com.smartordering.modules.system.service;

import com.smartordering.common.result.PageResult;
import com.smartordering.modules.system.dto.LoginLogQueryDTO;
import com.smartordering.modules.system.dto.OperationLogQueryDTO;
import com.smartordering.modules.system.entity.SysOperationLog;
import com.smartordering.modules.system.vo.LoginLogVO;
import com.smartordering.modules.system.vo.OperationLogVO;

/**
 * Sys log query service interface.
 *
 * @author smartordering
 */
public interface SysLogService {

    PageResult<LoginLogVO> pageLoginLogs(LoginLogQueryDTO dto);

    PageResult<OperationLogVO> pageOperationLogs(OperationLogQueryDTO dto);

    /** 记录一条登录日志（成功/失败都记） */
    void recordLogin(String username, String ip, String browser, String os, int status, String message);

    /** 记录一条操作日志（由切面采集组装后落库） */
    void recordOperation(SysOperationLog record);
}