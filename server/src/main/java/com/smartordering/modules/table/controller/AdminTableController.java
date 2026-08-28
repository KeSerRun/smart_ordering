package com.smartordering.modules.table.controller;

import com.smartordering.common.result.Result;
import com.smartordering.modules.table.dto.TableCreateDTO;
import com.smartordering.modules.table.dto.TableUpdateDTO;
import com.smartordering.modules.table.service.DiningTableService;
import com.smartordering.modules.table.vo.DiningTableVO;
import com.smartordering.modules.table.vo.QrCodeTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin dining table controller.
 *
 * @author smartordering
 */
@Tag(name = "桌台管理 (Admin)")
@RestController
@RequestMapping("/admin/table")
@RequiredArgsConstructor
public class AdminTableController {

    private final DiningTableService diningTableService;

    @Operation(summary = "List all tables (board view)")
    @GetMapping("/list")
    public Result<List<DiningTableVO>> list() {
        return Result.success(diningTableService.listAll());
    }

    @Operation(summary = "Create table")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody TableCreateDTO dto) {
        diningTableService.createTable(dto);
        return Result.success();
    }

    @Operation(summary = "Update table")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TableUpdateDTO dto) {
        dto.setId(id);
        diningTableService.updateTable(dto);
        return Result.success();
    }

    @Operation(summary = "Delete table")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        diningTableService.deleteTable(id);
        return Result.success();
    }

    @Operation(summary = "Table current order (placeholder)")
    @GetMapping("/{id}/order")
    public Result<Object> getTableOrder(@PathVariable Long id) {
        return Result.success(null);
    }

    @Operation(summary = "Mark table clean")
    @PutMapping("/{id}/clean")
    public Result<Void> markClean(@PathVariable Long id) {
        diningTableService.markClean(id);
        return Result.success();
    }

    @Operation(summary = "Confirm checkout (occupied -> to-clean)")
    @PutMapping("/{id}/checkout")
    public Result<Void> checkout(@PathVariable Long id) {
        diningTableService.checkoutTableIfSettled(id);
        return Result.success();
    }

    @Operation(summary = "Move table to to-clean")
    @PutMapping("/{id}/to-clean")
    public Result<Void> toClean(@PathVariable Long id) {
        diningTableService.updateTableStatus(id, 3);
        return Result.success();
    }

    @Operation(summary = "Release table (free)")
    @PutMapping("/{id}/release")
    public Result<Void> release(@PathVariable Long id) {
        diningTableService.releaseTable(id);
        return Result.success();
    }

    @Operation(summary = "Download table QR code")
    @GetMapping("/{id}/qrcode/download")
    public void downloadQrCode(@PathVariable Long id, HttpServletResponse response) {
        diningTableService.downloadQrCode(id, response);
    }

    @Operation(summary = "Delete table QR code (state -> false)")
    @DeleteMapping("/{id}/qrcode")
    public Result<Void> deleteQrCode(@PathVariable Long id) {
        diningTableService.deleteTableQrCode(id);
        return Result.success();
    }

    @Operation(summary = "Generate all table QR codes")
    @PostMapping("/qrcode/generate-all")
    public Result<Integer> generateAllQrCodes() {
        return Result.success(diningTableService.generateAllQrCodes());
    }

    @Operation(summary = "Async generate all QR codes task")
    @PostMapping("/qrcode/generate-all/task")
    public Result<QrCodeTaskVO> submitGenerateAllQrCodesTask() {
        return Result.success(diningTableService.submitGenerateAllQrCodesTask());
    }

    @Operation(summary = "Async download-all QR codes task")
    @PostMapping("/qrcode/download-all/task")
    public Result<QrCodeTaskVO> submitDownloadAllQrCodesTask() {
        return Result.success(diningTableService.submitDownloadAllQrCodesTask());
    }

    @Operation(summary = "Query QR task status")
    @GetMapping("/qrcode/task/{taskId}")
    public Result<QrCodeTaskVO> getQrCodeTask(@PathVariable String taskId) {
        return Result.success(diningTableService.getQrCodeTask(taskId));
    }

    @Operation(summary = "Download QR task result file")
    @GetMapping("/qrcode/task/{taskId}/download")
    public void downloadQrCodeTaskFile(@PathVariable String taskId, HttpServletResponse response) {
        diningTableService.downloadQrCodeTaskFile(taskId, response);
    }
}