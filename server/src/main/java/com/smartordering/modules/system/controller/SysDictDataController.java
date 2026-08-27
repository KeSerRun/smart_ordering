package com.smartordering.modules.system.controller;

import com.smartordering.common.result.Result;
import com.smartordering.modules.system.dto.DictDataCreateDTO;
import com.smartordering.modules.system.dto.DictDataUpdateDTO;
import com.smartordering.modules.system.service.SysDictDataService;
import com.smartordering.modules.system.vo.DictDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin dict data controller.
 *
 * @author smartordering
 */
@Tag(name = "字典数据 (Admin)")
@RestController
@RequestMapping("/system/dict/data")
@RequiredArgsConstructor
public class SysDictDataController {

    private final SysDictDataService dictDataService;

    @Operation(summary = "Dict data by type id")
    @GetMapping("/type/{typeId}")
    public Result<List<DictDataVO>> getByTypeId(@PathVariable Long typeId) {
        return Result.success(dictDataService.getByTypeId(typeId));
    }

    @Operation(summary = "Dict data by type code")
    @GetMapping("/code/{typeCode}")
    public Result<List<DictDataVO>> getByTypeCode(@PathVariable String typeCode) {
        return Result.success(dictDataService.getByTypeCode(typeCode));
    }

    @Operation(summary = "Dict data detail")
    @GetMapping("/{dictDataId}")
    public Result<DictDataVO> getInfo(@PathVariable Long dictDataId) {
        return Result.success(dictDataService.getInfo(dictDataId));
    }

    @Operation(summary = "Create dict data")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody DictDataCreateDTO dto) {
        dictDataService.create(dto);
        return Result.success();
    }

    @Operation(summary = "Update dict data")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody DictDataUpdateDTO dto) {
        dictDataService.update(dto);
        return Result.success();
    }

    @Operation(summary = "Delete dict data")
    @DeleteMapping("/{dictDataId}")
    public Result<Void> delete(@PathVariable Long dictDataId) {
        dictDataService.delete(dictDataId);
        return Result.success();
    }
}