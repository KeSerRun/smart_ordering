package com.smartordering.modules.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.smartordering.modules.system.entity.SysMenu;
import com.smartordering.modules.system.mapper.SysMenuMapper;
import com.smartordering.modules.system.service.PermissionCacheService;
import com.smartordering.modules.system.service.SysMenuService;
import com.smartordering.modules.system.vo.RouteVO;
import com.smartordering.modules.system.vo.UserRouteVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * System menu service implementation
 *
 * @author smartordering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    /** Menu type: 2 = button (excluded from routes) */
    private static final int TYPE_BUTTON = 2;

    private final SysMenuMapper sysMenuMapper;
    private final PermissionCacheService permissionCacheService;

    @Override
    public UserRouteVO getUserRoutes(Long userId) {
        // 1. Query menus authorized to this user (user -> role -> menu join)
        List<SysMenu> menus = sysMenuMapper.selectMenusByUserId(userId);

        // 2. Exclude buttons, keep directory (0) and menu (1) only
        List<SysMenu> filteredMenus = menus.stream()
                .filter(m -> m.getType() == null || m.getType() != TYPE_BUTTON)
                .collect(Collectors.toList());

        // 3. Build route tree
        List<RouteVO> routes = buildRouteTree(filteredMenus);

        // 4. Inject hidden routes (e.g. order detail page, not managed in DB)
        injectHiddenRoutes(routes);

        // 5. Add home route at first position
        RouteVO homeRoute = new RouteVO();
        homeRoute.setName("home");
        homeRoute.setPath("/home");
        homeRoute.setComponent("layout.base$view.home");
        homeRoute.setId("home");
        RouteVO.RouteMeta homeMeta = new RouteVO.RouteMeta();
        homeMeta.setTitle("首页");
        homeMeta.setI18nKey("route.home");
        homeMeta.setIcon("mdi:monitor-dashboard");
        homeMeta.setOrder(0);
        homeRoute.setMeta(homeMeta);
        routes.add(0, homeRoute);

        // 6. Resolve home route by role
        UserRouteVO result = new UserRouteVO();
        result.setRoutes(routes);
        result.setHome(resolveHomeRoute(userId, routes));
        return result;
    }

    // ==================== private ====================

    /**
     * Resolve home route by role, fallback to first available leaf route.
     */
    private String resolveHomeRoute(Long userId, List<RouteVO> routes) {
        List<String> roleCodes = permissionCacheService.getUserRoles(userId);

        // Restaurant admin keeps the operational home page
        if (roleCodes.contains("admin")) {
            return "home";
        }
        if (roleCodes.contains("waiter")) {
            return pickFirstAvailableRoute(routes, List.of("service_table-board", "service_place-order", "service_order-ops"));
        }
        if (roleCodes.contains("cashier")) {
            return pickFirstAvailableRoute(routes, List.of("service_order-ops", "service_table-board", "service_place-order"));
        }
        if (roleCodes.contains("kitchen")) {
            return pickFirstAvailableRoute(routes, List.of("service_kitchen"));
        }
        return pickFirstAvailableRoute(routes, List.of("home"));
    }

    /**
     * Pick the first preferred route the user actually has; fallback to home or first leaf.
     */
    private String pickFirstAvailableRoute(List<RouteVO> routes, List<String> preferredRouteNames) {
        List<String> availableRouteNames = flattenLeafRouteNames(routes);
        for (String preferredRouteName : preferredRouteNames) {
            if (availableRouteNames.contains(preferredRouteName)) {
                return preferredRouteName;
            }
        }
        if (availableRouteNames.contains("home")) {
            return "home";
        }
        return CollUtil.isNotEmpty(availableRouteNames) ? availableRouteNames.get(0) : "home";
    }

    private List<String> flattenLeafRouteNames(List<RouteVO> routes) {
        List<String> routeNames = new ArrayList<>();
        collectLeafRouteNames(routes, routeNames);
        return routeNames;
    }

    private void collectLeafRouteNames(List<RouteVO> routes, List<String> routeNames) {
        if (CollUtil.isEmpty(routes)) {
            return;
        }
        for (RouteVO route : routes) {
            if (CollUtil.isNotEmpty(route.getChildren())) {
                collectLeafRouteNames(route.getChildren(), routeNames);
                continue;
            }
            if (StrUtil.isNotBlank(route.getName())) {
                routeNames.add(route.getName());
            }
        }
    }

    /**
     * Build route tree from menu list (adapted for Soybean Admin).
     */
    private List<RouteVO> buildRouteTree(List<SysMenu> menus) {
        if (CollUtil.isEmpty(menus)) {
            return new ArrayList<>();
        }

        // Root menus: parentId is null or 0
        List<SysMenu> rootMenus = menus.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .sorted((a, b) -> orderOf(a) - orderOf(b))
                .collect(Collectors.toList());

        List<RouteVO> routes = new ArrayList<>();
        for (SysMenu rootMenu : rootMenus) {
            RouteVO route = convertToRoute(rootMenu, menus);
            if (route != null) {
                routes.add(route);
            }
        }
        return routes;
    }

    private RouteVO convertToRoute(SysMenu menu, List<SysMenu> allMenus) {
        RouteVO route = new RouteVO();
        String routeName = generateRouteName(menu);
        route.setName(routeName);
        route.setPath(menu.getPath());
        route.setId(String.valueOf(menu.getId()));

        // Meta
        RouteVO.RouteMeta meta = new RouteVO.RouteMeta();
        meta.setTitle(menu.getName());
        meta.setIcon(menu.getIcon());
        meta.setOrder(menu.getOrderNum());
        route.setMeta(meta);

        // Children
        List<SysMenu> children = allMenus.stream()
                .filter(m -> menu.getId().equals(m.getParentId()))
                .sorted((a, b) -> orderOf(a) - orderOf(b))
                .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(children)) {
            // Directory: has children
            route.setComponent("layout.base");
            List<RouteVO> childRoutes = new ArrayList<>();
            for (SysMenu child : children) {
                RouteVO childRoute = convertToRoute(child, allMenus);
                if (childRoute != null) {
                    childRoutes.add(childRoute);
                }
            }
            route.setChildren(childRoutes);
        } else {
            // Leaf page: component from path, not DB component field
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                route.setComponent("layout.base$view." + routeName);
            } else {
                route.setComponent("view." + routeName);
            }
        }

        // Drop invalid nodes (no component and no children)
        if (StrUtil.isBlank(route.getComponent()) && CollUtil.isEmpty(route.getChildren())) {
            return null;
        }
        return route;
    }

    private String generateRouteName(SysMenu menu) {
        String path = menu.getPath();
        if (StrUtil.isBlank(path)) {
            return "menu_" + menu.getId();
        }
        // /system/user -> manage_user ; /system -> manage ; /log/login -> log_login
        String name = path.replaceFirst("^/", "").replace("/", "_");
        if (name.startsWith("system_")) {
            name = "manage_" + name.substring("system_".length());
        } else if (name.equals("system")) {
            name = "manage";
        }
        return name;
    }

    /**
     * Inject hidden routes (e.g. order detail page) into the route tree.
     */
    private void injectHiddenRoutes(List<RouteVO> routes) {
        for (RouteVO route : routes) {
            if ("order".equals(route.getName()) && route.getChildren() != null) {
                RouteVO detailRoute = new RouteVO();
                detailRoute.setName("order_detail");
                detailRoute.setPath("/order/detail/:id");
                detailRoute.setComponent("view.order_detail");
                detailRoute.setProps(true);
                detailRoute.setId("order_detail");
                RouteVO.RouteMeta detailMeta = new RouteVO.RouteMeta();
                detailMeta.setTitle("订单详情");
                detailMeta.setI18nKey("route.order_detail");
                detailMeta.setHideInMenu(true);
                detailMeta.setActiveMenu("order_list");
                detailRoute.setMeta(detailMeta);
                route.getChildren().add(detailRoute);
                break;
            }
        }
    }

    private int orderOf(SysMenu menu) {
        return menu.getOrderNum() != null ? menu.getOrderNum() : 0;
    }
}