package com.smartordering.modules.table.controller;

import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.table.service.DiningTableService;
import com.smartordering.modules.table.vo.DiningTableVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dining table controller (app side)
 *
 * @author smartordering
 */
@Tag(name = "Table (App)")
@RestController
@RequestMapping("/app/table")
@RequiredArgsConstructor
public class AppTableController {

    private final DiningTableService diningTableService;

    @Operation(summary = "Get table by code (scan QR to enter)")
    @GetMapping("/{code}")
    public ApiResponse<DiningTableVO> getByCode(@PathVariable String code) {
        return ApiResponse.ok(diningTableService.getByCode(code));
    }

    @Operation(summary = "List tables (optional areaId)")
    @GetMapping("/list")
    public ApiResponse<List<DiningTableVO>> list(@RequestParam(required = false) Long areaId) {
        return ApiResponse.ok(diningTableService.list(areaId));
    }
}