package com.smartordering.modules.system.controller;

import com.smartordering.common.result.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口 —— 验证统一返回体和全局异常处理是否生效
 *
 * @author smartordering
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /** 正常返回 */
    @GetMapping("/ok")
    public ApiResponse<String> ok() {
        return ApiResponse.ok("骨架搭建成功");
    }

    /** 主动抛业务异常，验证全局异常处理 */
    @GetMapping("/error")
    public ApiResponse<String> error() {
        throw new com.smartordering.common.exception.BusinessException("这是一个测试业务异常");
    }
}