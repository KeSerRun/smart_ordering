package com.smartordering.modules.system.controller;

import com.smartordering.common.result.Result;
import com.smartordering.modules.system.dto.MenuCreateDTO;
import com.smartordering.modules.system.dto.MenuUpdateDTO;
import com.smartordering.modules.system.service.SysMenuAdminService;
import com.smartordering.modules.system.vo.MenuTreeVO;
import com.smartordering.modules.system.vo.MenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin menu management controller.
 *
 * @author smartordering
 */
@Tag(name = "菜单管理 (Admin)")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuAdminService menuService;

    @Operation(summary = "Paged-free flat menu list")
    @GetMapping("/list")
    public Result<List<MenuVO>> list() {
        return Result.success(menuService.listAll());
    }

    @Operation(summary = "Menu tree")
    @GetMapping("/tree")
    public Result<List<MenuTreeVO>> tree() {
        return Result.success(menuService.getMenuTree());
    }

    @Operation(summary = "Permission tree")
    @GetMapping("/permission/tree")
    public Result<List<MenuTreeVO>> permissionTree() {
        return Result.success(menuService.getPermissionTree());
    }

    @Operation(summary = "Current user's menu tree")
    @GetMapping("/user/tree")
    public Result<List<MenuTreeVO>> userTree() {
        return Result.success(menuService.getCurrentUserMenuTree());
    }

    @Operation(summary = "Create menu")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody MenuCreateDTO dto) {
        menuService.createMenu(dto);
        return Result.success();
    }

    @Operation(summary = "Update menu")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody MenuUpdateDTO dto) {
        menuService.updateMenu(dto);
        return Result.success();
    }

    @Operation(summary = "Delete menu")
    @DeleteMapping("/{menuId}")
    public Result<Void> delete(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return Result.success();
    }
}