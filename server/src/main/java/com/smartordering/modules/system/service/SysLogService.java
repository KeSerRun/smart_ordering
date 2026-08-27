package com.smartordering.modules.system.service;

import com.smartordering.common.result.PageResult;
import com.smartordering.modules.system.dto.LoginLogQueryDTO;
import com.smartordering.modules.system.dto.OperationLogQueryDTO;
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
}