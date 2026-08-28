package com.smartordering.modules.banner.controller;

import com.smartordering.common.result.PageResult;
import com.smartordering.common.result.Result;
import com.smartordering.modules.banner.dto.HomeBannerCreateDTO;
import com.smartordering.modules.banner.dto.HomeBannerQueryDTO;
import com.smartordering.modules.banner.dto.HomeBannerUpdateDTO;
import com.smartordering.modules.banner.service.HomeBannerService;
import com.smartordering.modules.banner.vo.HomeBannerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Admin home banner controller.
 *
 * @author smartordering
 */
@Tag(name = "首页轮播图 (Admin)")
@RestController
@RequestMapping("/admin/banner")
@RequiredArgsConstructor
public class AdminHomeBannerController {

    private final HomeBannerService homeBannerService;

    @Operation(summary = "Paged banner list")
    @GetMapping("/page")
    public Result<PageResult<HomeBannerVO>> pageList(HomeBannerQueryDTO dto) {
        return Result.success(homeBannerService.pageList(dto));
    }

    @Operation(summary = "Create banner")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody HomeBannerCreateDTO dto) {
        homeBannerService.create(dto);
        return Result.success();
    }

    @Operation(summary = "Update banner")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody HomeBannerUpdateDTO dto) {
        dto.setId(id);
        homeBannerService.update(dto);
        return Result.success();
    }

    @Operation(summary = "Update banner status")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        homeBannerService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "Delete banner (logic delete)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        homeBannerService.delete(id);
        return Result.success();
    }
}