package com.smartordering.modules.system.controller;

import com.smartordering.common.result.PageResult;
import com.smartordering.common.result.Result;
import com.smartordering.modules.system.dto.AdminResetPasswordDTO;
import com.smartordering.modules.system.dto.AdminUserUpdateDTO;
import com.smartordering.modules.system.dto.PasswordUpdateDTO;
import com.smartordering.modules.system.dto.UserCreateDTO;
import com.smartordering.modules.system.dto.UserQueryDTO;
import com.smartordering.modules.system.dto.UserUpdateDTO;
import com.smartordering.modules.system.service.SysUserService;
import com.smartordering.modules.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin user management controller.
 *
 * @author smartordering
 */
@Tag(name = "用户管理 (Admin)")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @Operation(summary = "Paged user list")
    @GetMapping("/page")
    public Result<PageResult<UserVO>> page(UserQueryDTO dto) {
        return Result.success(userService.pageList(dto));
    }

    @Operation(summary = "Update current user's profile")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUserInfo(dto);
        return Result.success();
    }

    @Operation(summary = "Change own password")
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        userService.updatePassword(dto);
        return Result.success();
    }

    @Operation(summary = "Admin reset a user's password")
    @PutMapping("/{userId}/password/reset")
    public Result<Void> resetPassword(@PathVariable Long userId,
                                      @Valid @RequestBody AdminResetPasswordDTO dto) {
        userService.resetPassword(userId, dto);
        return Result.success();
    }

    @Operation(summary = "Enable/disable a user")
    @PutMapping("/{userId}/status/{status}")
    public Result<Void> updateStatus(@PathVariable Long userId, @PathVariable Integer status) {
        userService.updateStatus(userId, status);
        return Result.success();
    }

    @Operation(summary = "Create user (with module grants)")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserCreateDTO dto) {
        return Result.success(userService.createUser(dto));
    }

    @Operation(summary = "Update user profile + module grants")
    @PutMapping("/{userId}")
    public Result<Void> update(@PathVariable Long userId, @Valid @RequestBody AdminUserUpdateDTO dto) {
        dto.setId(userId);
        userService.updateUser(dto);
        return Result.success();
    }

    @Operation(summary = "Delete user (admin/self protected)")
    @DeleteMapping("/{userId}")
    public Result<Void> delete(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return Result.success();
    }

    @Operation(summary = "Replace user's module grants (core/ops/sys/kitchen)")
    @PutMapping("/{userId}/modules")
    public Result<Void> updateModules(@PathVariable Long userId, @RequestBody List<String> modules) {
        userService.updateUserModules(userId, modules);
        return Result.success();
    }
}