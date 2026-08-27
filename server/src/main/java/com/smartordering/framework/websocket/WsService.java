package com.smartordering.framework.websocket;

import com.smartordering.common.enums.WsEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket 消息发送服务（嵌入式直发版）
 * <p>
 * 单机部署直接通过 {@link SimpMessagingTemplate} 推送至 STOMP 客户端，
 * 无需 Redis Pub/Sub 中转。
 *
 * @author smartordering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WsService {

    private final SimpMessagingTemplate messagingTemplate;

    /** 默认广播频道（单机下仅保留语义） */
    public static final String DEFAULT_CHANNEL = "broadcast";

    /** 向指定 topic 广播消息 */
    public void broadcast(WsEventType eventType, String topic, Object data) {
        broadcast(DEFAULT_CHANNEL, eventType, topic, data);
    }

    /** 向指定频道和 topic 广播消息（单机下 channel 仅作日志区分） */
    public void broadcast(String channel, WsEventType eventType, String topic, Object data) {
        try {
            WsMessage message = WsMessage.of(eventType, topic, data);
            messagingTemplate.convertAndSend(topic, message);
            log.debug("Broadcast WebSocket message to topic [{}], event: {}", topic, eventType);
        } catch (Exception e) {
            log.error("Failed to broadcast WebSocket message", e);
        }
    }
}