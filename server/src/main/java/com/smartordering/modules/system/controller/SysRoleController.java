package com.smartordering.modules.system.controller;

import com.smartordering.common.result.PageResult;
import com.smartordering.common.result.Result;
import com.smartordering.modules.system.dto.RoleCreateDTO;
import com.smartordering.modules.system.dto.RoleQueryDTO;
import com.smartordering.modules.system.dto.RoleUpdateDTO;
import com.smartordering.modules.system.service.SysRoleService;
import com.smartordering.modules.system.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin role management controller.
 *
 * @author smartordering
 */
@Tag(name = "角色管理 (Admin)")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @Operation(summary = "Paged role list")
    @GetMapping("/page")
    public Result<PageResult<RoleVO>> page(RoleQueryDTO dto) {
        return Result.success(roleService.pageList(dto));
    }

    @Operation(summary = "All roles")
    @GetMapping("/list")
    public Result<List<RoleVO>> list() {
        return Result.success(roleService.listAll());
    }

    @Operation(summary = "Role detail")
    @GetMapping("/{roleId}")
    public Result<RoleVO> getInfo(@PathVariable Long roleId) {
        return Result.success(roleService.getInfo(roleId));
    }

    @Operation(summary = "Create role")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody RoleCreateDTO dto) {
        roleService.createRole(dto);
        return Result.success();
    }

    @Operation(summary = "Update role")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody RoleUpdateDTO dto) {
        roleService.updateRole(dto);
        return Result.success();
    }

    @Operation(summary = "Delete role")
    @DeleteMapping("/{roleId}")
    public Result<Void> delete(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return Result.success();
    }

    @Operation(summary = "Assign menus to role")
    @PostMapping("/{roleId}/menus")
    public Result<Void> assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(roleId, menuIds);
        return Result.success();
    }

    @Operation(summary = "Role's menu ids")
    @GetMapping("/{roleId}/menus")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long roleId) {
        return Result.success(roleService.getRoleMenuIds(roleId));
    }

    @Operation(summary = "Update role status")
    @PutMapping("/{roleId}/status/{status}")
    public Result<Void> updateStatus(@PathVariable Long roleId, @PathVariable Integer status) {
        roleService.updateStatus(roleId, status);
        return Result.success();
    }

    @Operation(summary = "Assign users to role")
    @PostMapping("/{roleId}/users")
    public Result<Void> assignUsers(@PathVariable Long roleId, @RequestBody List<Long> userIds) {
        roleService.assignUsers(roleId, userIds);
        return Result.success();
    }

    @Operation(summary = "Role's user ids")
    @GetMapping("/{roleId}/users")
    public Result<List<Long>> getRoleUserIds(@PathVariable Long roleId) {
        return Result.success(roleService.getRoleUserIds(roleId));
    }
}