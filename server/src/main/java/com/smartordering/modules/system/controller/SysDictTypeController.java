package com.smartordering.modules.system.controller;

import com.smartordering.common.result.PageResult;
import com.smartordering.common.result.Result;
import com.smartordering.modules.system.dto.DictTypeCreateDTO;
import com.smartordering.modules.system.dto.DictTypeQueryDTO;
import com.smartordering.modules.system.dto.DictTypeUpdateDTO;
import com.smartordering.modules.system.service.SysDictTypeService;
import com.smartordering.modules.system.vo.DictTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin dict type controller.
 *
 * @author smartordering
 */
@Tag(name = "字典类型 (Admin)")
@RestController
@RequestMapping("/system/dict/type")
@RequiredArgsConstructor
public class SysDictTypeController {

    private final SysDictTypeService dictTypeService;

    @Operation(summary = "Paged dict type list")
    @GetMapping("/page")
    public Result<PageResult<DictTypeVO>> page(DictTypeQueryDTO dto) {
        return Result.success(dictTypeService.pageList(dto));
    }

    @Operation(summary = "All dict types")
    @GetMapping("/list")
    public Result<List<DictTypeVO>> list() {
        return Result.success(dictTypeService.listAll());
    }

    @Operation(summary = "Dict type detail")
    @GetMapping("/{dictTypeId}")
    public Result<DictTypeVO> getInfo(@PathVariable Long dictTypeId) {
        return Result.success(dictTypeService.getInfo(dictTypeId));
    }

    @Operation(summary = "Create dict type")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody DictTypeCreateDTO dto) {
        dictTypeService.create(dto);
        return Result.success();
    }

    @Operation(summary = "Update dict type")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody DictTypeUpdateDTO dto) {
        dictTypeService.update(dto);
        return Result.success();
    }

    @Operation(summary = "Delete dict type")
    @DeleteMapping("/{dictTypeId}")
    public Result<Void> delete(@PathVariable Long dictTypeId) {
        dictTypeService.delete(dictTypeId);
        return Result.success();
    }
}