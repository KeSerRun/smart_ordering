package com.smartordering.framework.satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.smartordering.modules.system.service.PermissionCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token permission interface implementation
 *
 * @author smartordering
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final PermissionCacheService permissionCacheService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return permissionCacheService.getUserPermissions(Long.valueOf(loginId.toString()));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return permissionCacheService.getUserRoles(Long.valueOf(loginId.toString()));
    }
}