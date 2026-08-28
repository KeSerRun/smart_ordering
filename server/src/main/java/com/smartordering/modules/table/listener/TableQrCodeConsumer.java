package com.smartordering.modules.table.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartordering.modules.mq.service.ReliableMessageService;
import com.smartordering.modules.table.dto.TableQrCodeEvent;
import com.smartordering.modules.table.service.DiningTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 桌台二维码批量生成任务消费者
 *
 * <p>监听 RabbitMQ 的 {@code table.qrcode.queue}（routing key: table.qrcode.generate），
 * 收到「生成全部桌台二维码」任务后异步批量生成：</p>
 * <ol>
 *   <li>先经 {@link ReliableMessageService#startConsume} 用 mq_consume_log 唯一键幂等去重，
 *       重复投递直接跳过，ack 不重复处理；</li>
 *   <li>遍历桌台生成二维码（复用 {@link DiningTableService#generateAllQrCodes()} 同步逻辑）；</li>
 *   <li>完成后回写任务状态（SUCCESS / FAILED），管理端前端轮询据此收敛并提示。</li>
 * </ol>
 * <p>处理失败不重抛（避免无限重投），记 failure 到 mq_consume_log 供排查。</p>
 *
 * @author smartordering
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TableQrCodeConsumer {

    /** 消费者组：桌台二维码 */
    private static final String CONSUMER_GROUP = "table-qrcode";

    private final ObjectMapper objectMapper;
    private final ReliableMessageService reliableMessageService;
    private final DiningTableService diningTableService;

    @RabbitListener(queues = "${smart.mq.table-qrcode-queue}")
    public void onGenerateAll(String payload) {
        TableQrCodeEvent event = parsePayload(payload);
        if (event == null || event.getTaskId() == null) {
            log.warn("Ignored invalid table QR event payload: {}", payload);
            return;
        }

        Long consumeId = reliableMessageService.startConsume(
                CONSUMER_GROUP,
                "table.qrcode.generate",
                "GEN_ALL_QR",
                event.getMessageKey(),
                event.getTaskId());
        if (consumeId == null) {
            // 幂等：该消息已被本消费者组处理过，跳过
            log.debug("Table QR event already consumed, skip: messageKey={}", event.getMessageKey());
            return;
        }

        try {
            int done = diningTableService.generateAllQrCodes();
            diningTableService.completeGenerateAllQrTask(event.getTaskId(),
                    event.getTotal(), done, null);
            reliableMessageService.finishConsume(consumeId, true, null);
            log.info("Table QR generate consumed: taskId={}, total={}, completed={}",
                    event.getTaskId(), event.getTotal(), done);
        } catch (Exception e) {
            log.error("Table QR generate consume failed: taskId={}", event.getTaskId(), e);
            diningTableService.completeGenerateAllQrTask(event.getTaskId(),
                    event.getTotal(), 0, e.getMessage());
            reliableMessageService.finishConsume(consumeId, false, e.getMessage());
        }
    }

    private TableQrCodeEvent parsePayload(String payload) {
        try {
            return objectMapper.readValue(payload, TableQrCodeEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse table QR event payload", e);
            return null;
        }
    }
}