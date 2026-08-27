package com.smartordering.modules.system.service.impl;

import com.smartordering.modules.system.mapper.SysUserMapper;
import com.smartordering.modules.system.service.PermissionCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Permission cache service implementation.
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
public class PermissionCacheServiceImpl implements PermissionCacheService {

    private final SysUserMapper sysUserMapper;

    @Override
    @Cacheable(value = "userRoles", key = "#userId")
    public List<String> getUserRoles(Long userId) {
        return sysUserMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    @Cacheable(value = "userPermissions", key = "#userId")
    public List<String> getUserPermissions(Long userId) {
        return sysUserMapper.selectPermissionsByUserId(userId);
    }

    @Override
    @CacheEvict(value = {"userRoles", "userPermissions"}, key = "#userId")
    public void clearUserCache(Long userId) {
        // no-op body; cache eviction happens via annotation
    }
}