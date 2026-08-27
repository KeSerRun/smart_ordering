package com.smartordering.modules.mq.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.mq.dto.MqMessageQueryDTO;
import com.smartordering.modules.mq.service.ReliableMessageService;
import com.smartordering.modules.mq.vo.MqMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * MQ message controller (admin side)
 *
 * @author smartordering
 */
@Tag(name = "MQ Message (Admin)")
@RestController
@RequestMapping("/admin/mq/message")
@RequiredArgsConstructor
public class AdminMqMessageController {

    private final ReliableMessageService reliableMessageService;

    @Operation(summary = "Page messages")
    @GetMapping("/page")
    public ApiResponse<IPage<MqMessageVO>> page(MqMessageQueryDTO dto) {
        return ApiResponse.ok(reliableMessageService.pageMessages(dto));
    }

    @Operation(summary = "Retry message")
    @PostMapping("/{id}/retry")
    public ApiResponse<Void> retry(@PathVariable Long id) {
        reliableMessageService.retryMessage(id);
        return ApiResponse.ok();
    }
}