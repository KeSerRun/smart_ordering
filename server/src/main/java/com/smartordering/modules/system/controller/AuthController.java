package com.smartordering.modules.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.system.dto.LoginDTO;
import com.smartordering.modules.system.dto.RegisterDTO;
import com.smartordering.modules.system.service.SysUserService;
import com.smartordering.modules.system.vo.LoginVO;
import com.smartordering.modules.system.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller.
 *
 * @author smartordering
 */
@Tag(name = "Authentication")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService userService;

    @Operation(summary = "Register")
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return ApiResponse.ok();
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return ApiResponse.ok(userService.login(dto));
    }

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        StpUtil.logout();
        return ApiResponse.ok();
    }

    @Operation(summary = "Get current user info")
    @GetMapping("/info")
    public ApiResponse<UserInfoVO> info() {
        return ApiResponse.ok(userService.getCurrentUserInfo());
    }

    @Operation(summary = "Refresh token (returns current valid token; renews timeout)")
    @GetMapping("/refreshToken")
    public ApiResponse<LoginVO> refreshToken() {
        if (!StpUtil.isLogin()) {
            return ApiResponse.error(401, "Not logged in");
        }
        StpUtil.renewTimeout(2592000);
        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setTokenName("Authorization");
        return ApiResponse.ok(vo);
    }

    @Operation(summary = "Report an auth error (frontend client-side logging)")
    @PostMapping("/error")
    public ApiResponse<Void> authError() {
        return ApiResponse.ok();
    }
}