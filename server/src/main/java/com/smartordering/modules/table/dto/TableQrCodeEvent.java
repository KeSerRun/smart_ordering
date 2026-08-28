package com.smartordering.modules.table.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 桌台二维码批量生成任务事件载荷
 *
 * <p>「生成全部桌台二维码」提交时写入发件箱并经 RabbitMQ 广播
 * （routing key: table.qrcode.generate），消费者据此在异步线程批量生成二维码。
 * payload 即本对象 JSON。</p>
 *
 * @author smartordering
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableQrCodeEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 消息唯一键，与 mq_message.message_key 一致（消费幂等依赖），即任务 taskId */
    private String messageKey;

    /** 二维码批量生成任务 ID，前端轮询状态用 */
    private String taskId;

    /** 本次任务待生成的桌台总数（提交时统计，供消费者回写任务状态） */
    private Integer total;
}