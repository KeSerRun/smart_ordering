package com.smartordering.modules.system.controller;

import com.smartordering.common.result.PageResult;
import com.smartordering.common.result.Result;
import com.smartordering.modules.system.dto.OperationLogQueryDTO;
import com.smartordering.modules.system.service.SysLogService;
import com.smartordering.modules.system.vo.OperationLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin operation log controller.
 *
 * @author smartordering
 */
@Tag(name = "操作日志 (Admin)")
@RestController
@RequestMapping("/system/log/operation")
@RequiredArgsConstructor
public class SysOperationLogController {

    private final SysLogService logService;

    @Operation(summary = "Paged operation logs")
    @GetMapping("/page")
    public Result<PageResult<OperationLogVO>> page(OperationLogQueryDTO dto) {
        return Result.success(logService.pageOperationLogs(dto));
    }
}