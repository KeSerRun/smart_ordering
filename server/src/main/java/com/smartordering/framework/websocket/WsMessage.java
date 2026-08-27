package com.smartordering.framework.websocket;

import com.smartordering.common.enums.WsEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * WebSocket 消息包装类
 *
 * @author smartordering
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 事件类型 */
    private WsEventType eventType;

    /** 消息数据（JSON 序列化后的业务数据） */
    private Object data;

    /** 目标 topic（如 /topic/kitchen, /topic/table-status） */
    private String topic;

    /** 消息发送时间 */
    private LocalDateTime timestamp;

    /** 创建消息的便捷方法 */
    public static WsMessage of(WsEventType eventType, String topic, Object data) {
        return WsMessage.builder()
                .eventType(eventType)
                .topic(topic)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}