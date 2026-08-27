package com.smartordering.modules.system.controller;

import com.smartordering.common.result.PageResult;
import com.smartordering.common.result.Result;
import com.smartordering.modules.system.dto.LoginLogQueryDTO;
import com.smartordering.modules.system.service.SysLogService;
import com.smartordering.modules.system.vo.LoginLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin login log controller.
 *
 * @author smartordering
 */
@Tag(name = "登录日志 (Admin)")
@RestController
@RequestMapping("/system/log/login")
@RequiredArgsConstructor
public class SysLoginLogController {

    private final SysLogService logService;

    @Operation(summary = "Paged login logs")
    @GetMapping("/page")
    public Result<PageResult<LoginLogVO>> page(LoginLogQueryDTO dto) {
        return Result.success(logService.pageLoginLogs(dto));
    }
}