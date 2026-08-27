package com.smartordering.modules.table.controller;

import com.smartordering.common.result.Result;
import com.smartordering.modules.table.dto.TableAreaCreateDTO;
import com.smartordering.modules.table.dto.TableAreaUpdateDTO;
import com.smartordering.modules.table.service.TableAreaService;
import com.smartordering.modules.table.vo.TableAreaVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin table area controller.
 *
 * @author smartordering
 */
@Tag(name = "桌台区域 (Admin)")
@RestController
@RequestMapping("/admin/table/area")
@RequiredArgsConstructor
public class AdminTableAreaController {

    private final TableAreaService tableAreaService;

    @Operation(summary = "List all areas")
    @GetMapping("/list")
    public Result<List<TableAreaVO>> list() {
        return Result.success(tableAreaService.listAll());
    }

    @Operation(summary = "List enabled areas")
    @GetMapping("/enabled-list")
    public Result<List<TableAreaVO>> enabledList() {
        return Result.success(tableAreaService.listEnabled());
    }

    @Operation(summary = "Create area")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody TableAreaCreateDTO dto) {
        tableAreaService.createArea(dto);
        return Result.success();
    }

    @Operation(summary = "Update area")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TableAreaUpdateDTO dto) {
        dto.setId(id);
        tableAreaService.updateArea(dto);
        return Result.success();
    }

    @Operation(summary = "Delete area")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tableAreaService.deleteArea(id);
        return Result.success();
    }
}