package com.smartordering.common.exception;

import lombok.Getter;

/**
 * 业务异常 —— 业务代码主动抛出，由全局异常处理器统一捕获
 *
 * @author smartordering
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务状态码 */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}