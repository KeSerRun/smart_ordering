package com.smartordering.common.result;

public class ResultCode {
    /** 成功 */
    public static final int SUCCESS = 200;

    /** 通用失败 */
    public static final int FAIL = 500;

    /** 未登录 / token 失效 */
    public static final int UNAUTHORIZED = 401;

    /** 无权限 */
    public static final int FORBIDDEN = 403;

    /** 参数错误 */
    public static final int PARAM_ERROR = 400;

    /** 业务异常（默认值，具体错误可自定义） */
    public static final int BUSINESS_ERROR = 500;
}
