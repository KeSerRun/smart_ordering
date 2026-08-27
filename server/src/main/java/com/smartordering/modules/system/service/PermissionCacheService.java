package com.smartordering.modules.system.service;

import java.util.List;

/**
 * Permission cache service interface
 *
 * @author smartordering
 */
public interface PermissionCacheService {

    List<String> getUserRoles(Long userId);

    List<String> getUserPermissions(Long userId);

    /** Evict a user's cached roles/permissions (on password reset or disable) */
    void clearUserCache(Long userId);
}