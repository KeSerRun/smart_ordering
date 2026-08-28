package com.smartordering.modules.mq.enums;

/**
 * MQ 消费状态枚举
 *
 * <p>对应 {@code mq_consume_log.consume_status} 列：</p>
 * <ul>
 *   <li>0 - 待消费：日志占位已写入（幂等键），业务处理进行中；</li>
 *   <li>1 - 消费成功：业务处理完成；</li>
 *   <li>2 - 消费失败：业务处理异常，记录 last_error 供排查。</li>
 * </ul>
 *
 * @author smartordering
 */
public enum MqConsumeStatus {

    /** 待消费 */
    INIT(0, "待消费"),

    /** 消费成功 */
    SUCCESS(1, "消费成功"),

    /** 消费失败 */
    FAILED(2, "消费失败");

    /** 状态码，与 mq_consume_log.consume_status 列对应 */
    private final int code;

    /** 状态描述 */
    private final String description;

    MqConsumeStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}