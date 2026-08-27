package com.smartordering.modules.system.controller;

import com.smartordering.common.result.PageResult;
import com.smartordering.common.result.Result;
import com.smartordering.modules.system.dto.ConfigCreateDTO;
import com.smartordering.modules.system.dto.ConfigQueryDTO;
import com.smartordering.modules.system.dto.ConfigUpdateDTO;
import com.smartordering.modules.system.service.SysConfigService;
import com.smartordering.modules.system.vo.ConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Admin config controller.
 *
 * @author smartordering
 */
@Tag(name = "系统配置 (Admin)")
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService configService;

    @Operation(summary = "Paged config list")
    @GetMapping("/page")
    public Result<PageResult<ConfigVO>> page(ConfigQueryDTO dto) {
        return Result.success(configService.pageList(dto));
    }

    @Operation(summary = "Config value by key")
    @GetMapping("/key/{configKey}")
    public Result<String> getByKey(@PathVariable String configKey) {
        return Result.success(configService.getByKey(configKey));
    }

    @Operation(summary = "Config detail")
    @GetMapping("/{configId}")
    public Result<ConfigVO> getInfo(@PathVariable Long configId) {
        return Result.success(configService.getInfo(configId));
    }

    @Operation(summary = "Get admin theme preset")
    @GetMapping("/theme-preset")
    public Result<String> getAdminThemePreset() {
        return Result.success(configService.getAdminThemePreset());
    }

    @Operation(summary = "Save admin theme preset")
    @PutMapping("/theme-preset/{presetId}")
    public Result<Void> saveAdminThemePreset(@PathVariable String presetId) {
        configService.saveAdminThemePreset(presetId);
        return Result.success();
    }

    @Operation(summary = "Create config")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ConfigCreateDTO dto) {
        configService.create(dto);
        return Result.success();
    }

    @Operation(summary = "Update config")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ConfigUpdateDTO dto) {
        configService.update(dto);
        return Result.success();
    }

    @Operation(summary = "Delete config")
    @DeleteMapping("/{configId}")
    public Result<Void> delete(@PathVariable Long configId) {
        configService.delete(configId);
        return Result.success();
    }
}