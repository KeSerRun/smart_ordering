package com.smartordering.modules.kitchen.controller;

import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.kitchen.service.KitchenService;
import com.smartordering.modules.kitchen.vo.KitchenTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kitchen controller (app side).
 *
 * @author smartordering
 */
@Tag(name = "Kitchen")
@RestController
@RequestMapping("/app/kitchen")
@RequiredArgsConstructor
public class AppKitchenController {

    private final KitchenService kitchenService;

    @Operation(summary = "Get task list (pending/cooking)")
    @GetMapping("/tasks")
    public ApiResponse<List<KitchenTaskVO>> getTaskList() {
        return ApiResponse.ok(kitchenService.getTaskList());
    }

    @Operation(summary = "Accept task (pending -> cooking)")
    @PutMapping("/task/{itemId}/accept")
    public ApiResponse<Void> acceptTask(@PathVariable Long itemId) {
        kitchenService.acceptTask(itemId);
        return ApiResponse.ok();
    }

    @Operation(summary = "Complete task (cooking -> done)")
    @PutMapping("/task/{itemId}/complete")
    public ApiResponse<Void> completeTask(@PathVariable Long itemId) {
        kitchenService.completeTask(itemId);
        return ApiResponse.ok();
    }

    @Operation(summary = "Serve task (done -> served)")
    @PutMapping("/task/{itemId}/serve")
    public ApiResponse<Void> serveTask(@PathVariable Long itemId) {
        kitchenService.serveItem(itemId);
        return ApiResponse.ok();
    }

    @Operation(summary = "Get auto-accept enabled flag")
    @GetMapping("/auto-accept")
    public ApiResponse<Boolean> getAutoAcceptEnabled() {
        return ApiResponse.ok(kitchenService.getAutoAcceptEnabled());
    }

    @Operation(summary = "Update auto-accept enabled flag")
    @PutMapping("/auto-accept")
    public ApiResponse<Void> updateAutoAcceptEnabled(@RequestParam boolean enabled) {
        kitchenService.updateAutoAcceptEnabled(enabled);
        return ApiResponse.ok();
    }
}