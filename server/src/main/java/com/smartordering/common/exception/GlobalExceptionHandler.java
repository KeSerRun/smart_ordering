package com.smartordering.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.smartordering.common.result.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler
 *
 * @author smartordering
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Business exception (expected) */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    /** Validation error (@Valid failed on request body) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst().orElse("Invalid parameter");
        log.warn("Validation error: {}", message);
        return ApiResponse.error(400, message);
    }

    /** Missing required request parameter */
    @ExceptionHandler(ServletRequestBindingException.class)
    public ApiResponse<Void> handleBindException(ServletRequestBindingException e) {
        log.warn("Bind error: {}", e.getMessage());
        return ApiResponse.error(400, e.getMessage());
    }

    /** Not logged in */
    @ExceptionHandler(NotLoginException.class)
    public ApiResponse<Void> handleNotLogin(NotLoginException e) {
        return ApiResponse.error(401, "Not logged in");
    }

    /** No permission */
    @ExceptionHandler(NotPermissionException.class)
    public ApiResponse<Void> handleNotPermission(NotPermissionException e) {
        return ApiResponse.error(403, "No permission");
    }

    /** No role */
    @ExceptionHandler(NotRoleException.class)
    public ApiResponse<Void> handleNotRole(NotRoleException e) {
        return ApiResponse.error(403, "No role");
    }

    /** No static resource (404): handles SPA history-mode URI mismatches */
    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<Void> handleNoResourceFound(NoResourceFoundException e) {
        return ApiResponse.error(404, "Resource not found");
    }

    /** Unexpected exception (500) */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("System exception: ", e);
        return ApiResponse.error(500, "System busy, please retry later");
    }
}