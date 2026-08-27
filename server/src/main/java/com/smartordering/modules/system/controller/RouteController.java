package com.smartordering.modules.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.system.service.SysMenuService;
import com.smartordering.modules.system.vo.RouteVO;
import com.smartordering.modules.system.vo.UserRouteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Route controller
 *
 * @author smartordering
 */
@Tag(name = "Route")
@RestController
@RequestMapping("/route")
@RequiredArgsConstructor
public class RouteController {

    private final SysMenuService sysMenuService;

    @Operation(summary = "Get constant routes (no login required)")
    @GetMapping("/getConstantRoutes")
    public ApiResponse<List<RouteVO>> getConstantRoutes() {
        List<RouteVO> routes = new ArrayList<>();

        // Login page
        RouteVO login = new RouteVO();
        login.setName("login");
        login.setPath("/login/:module(pwd-login|code-login|register|reset-pwd|bind-wechat)?");
        login.setComponent("layout.blank$view.login");
        login.setId("login");
        RouteVO.RouteMeta loginMeta = new RouteVO.RouteMeta();
        loginMeta.setTitle("login");
        loginMeta.setI18nKey("route.login");
        loginMeta.setConstant(true);
        loginMeta.setHideInMenu(true);
        login.setMeta(loginMeta);
        routes.add(login);

        // 403
        RouteVO forbidden = new RouteVO();
        forbidden.setName("403");
        forbidden.setPath("/403");
        forbidden.setComponent("layout.blank$view.403");
        forbidden.setId("403");
        RouteVO.RouteMeta forbiddenMeta = new RouteVO.RouteMeta();
        forbiddenMeta.setTitle("403");
        forbiddenMeta.setI18nKey("route.403");
        forbiddenMeta.setConstant(true);
        forbiddenMeta.setHideInMenu(true);
        forbidden.setMeta(forbiddenMeta);
        routes.add(forbidden);

        // 404
        RouteVO notFound = new RouteVO();
        notFound.setName("404");
        notFound.setPath("/404");
        notFound.setComponent("layout.blank$view.404");
        notFound.setId("404");
        RouteVO.RouteMeta notFoundMeta = new RouteVO.RouteMeta();
        notFoundMeta.setTitle("404");
        notFoundMeta.setI18nKey("route.404");
        notFoundMeta.setConstant(true);
        notFoundMeta.setHideInMenu(true);
        notFound.setMeta(notFoundMeta);
        routes.add(notFound);

        // 500
        RouteVO serverError = new RouteVO();
        serverError.setName("500");
        serverError.setPath("/500");
        serverError.setComponent("layout.blank$view.500");
        serverError.setId("500");
        RouteVO.RouteMeta serverErrorMeta = new RouteVO.RouteMeta();
        serverErrorMeta.setTitle("500");
        serverErrorMeta.setI18nKey("route.500");
        serverErrorMeta.setConstant(true);
        serverErrorMeta.setHideInMenu(true);
        serverError.setMeta(serverErrorMeta);
        routes.add(serverError);

        return ApiResponse.ok(routes);
    }

    @Operation(summary = "Get user routes (login required)")
    @GetMapping("/getUserRoutes")
    public ApiResponse<UserRouteVO> getUserRoutes() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.ok(sysMenuService.getUserRoutes(userId));
    }

    @Operation(summary = "Check whether a route exists")
    @GetMapping("/isRouteExist")
    public ApiResponse<Boolean> isRouteExist(@RequestParam String routeName) {
        boolean exists = "home".equals(routeName) || routeName.startsWith("manage_");
        return ApiResponse.ok(exists);
    }
}