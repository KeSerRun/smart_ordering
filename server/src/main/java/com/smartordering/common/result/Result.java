package com.smartordering.common.result;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Unified response wrapper (ported from the reference project).
 *
 * <p>Serialization shape is {@code {code, message, data}} — identical to {@link ApiResponse},
 * so ported admin controllers can return either wrapper without breaking the frontend.</p>
 *
 * @author smartordering
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer code;

    private String message;

    private T data;

    private Result() {
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return success("success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail() {
        return fail("Operation failed");
    }

    public static <T> Result<T> fail(String message) {
        return fail(ResultCode.FAIL, message);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}