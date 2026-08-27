package com.smartordering.modules.system.service;

import com.smartordering.modules.system.vo.UserRouteVO;

/**
 * System menu service interface
 *
 * @author smartordering
 */
public interface SysMenuService {

    /**
     * Get user routes (adapted for Soybean Admin front-end route format)
     *
     * @param userId current login user ID
     * @return user route data (routes tree + home route name)
     */
    UserRouteVO getUserRoutes(Long userId);
}