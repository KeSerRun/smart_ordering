package com.smartordering.modules.mq.enums;

/**
 * MQ 消息投递状态枚举
 *
 * <p>对应 {@code mq_message.deliver_status} 列：</p>
 * <ul>
 *   <li>0 - 待投递：已落入发件箱（事务性 outbox），尚未确认 RabbitMQ 收到；</li>
 *   <li>1 - 已投递：RabbitMQ 已接受消息；</li>
 *   <li>2 - 投递失败：超过最大重试次数，需人工介入。</li>
 * </ul>
 *
 * @author smartordering
 */
public enum MqMessageStatus {

    /** 待投递 */
    PENDING(0, "待投递"),

    /** 已投递 */
    SENT(1, "已投递"),

    /** 投递失败 */
    FAILED(2, "投递失败");

    /** 状态码，与 mq_message.deliver_status 列对应 */
    private final int code;

    /** 状态描述 */
    private final String description;

    MqMessageStatus(int code, String description) {
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