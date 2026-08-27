package com.smartordering.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回体 —— 所有接口返回的格式
 *
 * <p>前端 axios 拦截器会根据 code 字段判断业务是否成功</p>
 *
 * @param <T> data 的类型
 */
@Data   // getter, setter
@NoArgsConstructor  // 无参构造
@AllArgsConstructor // 全参构造
public class ApiResponse<T> {

    /** 业务状态码：200 成功，其他为异常 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 返回数据 */
    private T data;

    // ========== 静态工厂方法（成功） ==========

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    // ========== 静态工厂方法（失败） ==========

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(500, message);
    }
}