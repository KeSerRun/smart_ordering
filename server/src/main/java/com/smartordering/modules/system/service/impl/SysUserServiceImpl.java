package com.smartordering.modules.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.common.result.PageResult;
import com.smartordering.modules.system.dto.AdminResetPasswordDTO;
import com.smartordering.modules.system.dto.AdminUserUpdateDTO;
import com.smartordering.modules.system.dto.LoginDTO;
import com.smartordering.modules.system.dto.PasswordUpdateDTO;
import com.smartordering.modules.system.dto.RegisterDTO;
import com.smartordering.modules.system.dto.UserCreateDTO;
import com.smartordering.modules.system.dto.UserQueryDTO;
import com.smartordering.modules.system.dto.UserUpdateDTO;
import com.smartordering.modules.system.entity.SysRoleModule;
import com.smartordering.modules.system.entity.SysUser;
import com.smartordering.modules.system.entity.SysUserRole;
import com.smartordering.modules.system.mapper.SysRoleModuleMapper;
import com.smartordering.modules.system.mapper.SysUserMapper;
import com.smartordering.modules.system.mapper.SysUserRoleMapper;
import com.smartordering.modules.system.service.SysLogService;
import com.smartordering.modules.system.service.SysUserService;
import com.smartordering.modules.system.vo.LoginVO;
import com.smartordering.modules.system.vo.UserInfoVO;
import com.smartordering.modules.system.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User service implementation.
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    /** 内置超级管理员用户名：不可删除、不可禁用 */
    private static final String SUPER_ADMIN = "admin";

    private final SysUserMapper sysUserMapper;
        private final SysUserRoleMapper sysUserRoleMapper;
        private final SysRoleModuleMapper sysRoleModuleMapper;
        private final SysLogService sysLogService;
        private final PermissionCacheServiceImpl permissionCacheService;

    @Override
    public void register(RegisterDTO dto) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("Username already exists");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setStatus(1);
        sysUserMapper.insert(user);
    }

    @Override
        public LoginVO login(LoginDTO dto, HttpServletRequest request) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUsername, dto.getUsername());
            SysUser user = sysUserMapper.selectOne(wrapper);

            // 客户端信息（IP / 浏览器 / 操作系统），成功与失败均记录登录日志
                        // 注意：hutool 5.8 ServletUtil 依赖 javax.servlet，本项目为 jakarta（Spring Boot 3），手写 IP 提取
                        String ip = clientIp(request);
                        UserAgent ua = UserAgentUtil.parse(request.getHeader("User-Agent"));
            String browser = ua == null ? null : ua.getBrowser().getName();
            String os = ua == null ? null : ua.getOs().getName();

            if (user == null) {
                sysLogService.recordLogin(dto.getUsername(), ip, browser, os, 0, "账号不存在");
                throw new BusinessException("User not found");
            }
            if (user.getStatus() == 0) {
                sysLogService.recordLogin(dto.getUsername(), ip, browser, os, 0, "账号已禁用");
                throw new BusinessException("Account is disabled");
            }
            if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
                sysLogService.recordLogin(dto.getUsername(), ip, browser, os, 0, "密码错误");
                throw new BusinessException("Invalid password");
            }

            StpUtil.login(user.getId());
            sysLogService.recordLogin(user.getUsername(), ip, browser, os, 1, null);

            LoginVO vo = new LoginVO();
            vo.setToken(StpUtil.getTokenValue());
            vo.setTokenName("Authorization");
            vo.setUserInfo(buildUserInfo(user));
            return vo;
        }

    @Override
    public UserInfoVO getCurrentUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }
        return buildUserInfo(user);
    }

    @Override
    public PageResult<UserVO> pageList(UserQueryDTO dto) {
        long pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getUsername()), SysUser::getUsername, dto.getUsername())
                .eq(dto.getStatus() != null, SysUser::getStatus, dto.getStatus())
                .eq(StringUtils.hasText(dto.getUserType()), SysUser::getUserType, dto.getUserType())
                .ge(dto.getStartTime() != null, SysUser::getCreateTime, dto.getStartTime())
                .le(dto.getEndTime() != null, SysUser::getCreateTime, dto.getEndTime())
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = new Page<>(pageNum, pageSize);
        sysUserMapper.selectPage(page, wrapper);
        List<UserVO> list = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public void updateUserInfo(UserUpdateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = new SysUser();
        user.setId(userId);
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAvatar(dto.getAvatar());
        sysUserMapper.updateById(user);
    }

    @Override
    public void updatePassword(PasswordUpdateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }
        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("Old password is incorrect");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        sysUserMapper.updateById(update);
    }

    @Override
    public void resetPassword(Long userId, AdminResetPasswordDTO dto) {
        if (sysUserMapper.selectById(userId) == null) {
            throw new BusinessException("User not found");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        sysUserMapper.updateById(update);
        StpUtil.kickout(userId);
        permissionCacheService.clearUserCache(userId);
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (currentUserId.equals(userId) && status != null && status == 0) {
            throw new BusinessException("Cannot disable yourself");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setStatus(status);
        sysUserMapper.updateById(update);
        if (status != null && status == 0) {
            StpUtil.kickout(userId);
            permissionCacheService.clearUserCache(userId);
        }
    }

    @Override
    @Transactional
    public Long createUser(UserCreateDTO dto) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("Username already exists");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setUserType(StringUtils.hasText(dto.getUserType()) ? dto.getUserType() : "BACKEND");
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        sysUserMapper.insert(user);
        saveUserRoles(user.getId(), dto.getRoleIds());
        return user.getId();
    }

    @Override
    @Transactional
    public void updateUser(AdminUserUpdateDTO dto) {
        SysUser user = requireUser(dto.getId());
        if (SUPER_ADMIN.equals(user.getUsername())) {
            throw new BusinessException("内置管理员账号仅允许修改密码");
        }
        SysUser update = new SysUser();
        update.setId(dto.getId());
        update.setNickname(dto.getNickname());
        update.setEmail(dto.getEmail());
        update.setPhone(dto.getPhone());
        update.setStatus(dto.getStatus());
        sysUserMapper.updateById(update);
        if (dto.getRoleIds() != null) {
            saveUserRoles(dto.getId(), dto.getRoleIds());
            permissionCacheService.clearUserCache(dto.getId());
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (currentUserId.equals(userId)) {
            throw new BusinessException("Cannot delete yourself");
        }
        SysUser user = requireUser(userId);
        if (SUPER_ADMIN.equals(user.getUsername())) {
            throw new BusinessException("内置管理员账号不可删除");
        }
        // 物理删除用户 + 清理关联：用户-角色（逻辑删除会占用 uk_username，同名账户无法重建）
                sysUserMapper.physicalDeleteById(userId);
                sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId));
        StpUtil.kickout(userId);
        permissionCacheService.clearUserCache(userId);
    }

    // ==================== helpers ====================

    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setRoleIds(getUserRoleIds(user.getId()));
        vo.setModules(getUserModules(user.getId()));
        return vo;
    }

    private UserInfoVO buildUserInfo(SysUser user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRoles(permissionCacheService.getUserRoles(user.getId()));
        vo.setPermissions(permissionCacheService.getUserPermissions(user.getId()));
        vo.setModules(getUserModules(user.getId()));
        return vo;
    }

    private SysUser requireUser(Long userId) {
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("User not found");
            }
            return user;
        }

        /** 提取客户端 IP：X-Forwarded-For / X-Real-IP / RemoteAddr，兼容反向代理 */
        private String clientIp(HttpServletRequest request) {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }

    private List<Long> getUserRoleIds(Long userId) {
        return sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
    }

    /**
     * 用户可访问的模块 = 其所有角色的模块权限并集（RBAC：角色管权限，用户管账户+角色）
     */
    private List<String> getUserModules(Long userId) {
        List<Long> roleIds = getUserRoleIds(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return sysRoleModuleMapper.selectList(new LambdaQueryWrapper<SysRoleModule>()
                        .in(SysRoleModule::getRoleId, roleIds))
                .stream().map(SysRoleModule::getModuleCode).distinct().collect(Collectors.toList());
    }

    /** 全量替换用户角色（先删后插） */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds.stream().distinct().collect(Collectors.toList())) {
            SysUserRole row = new SysUserRole();
            row.setUserId(userId);
            row.setRoleId(roleId);
            sysUserRoleMapper.insert(row);
        }
    }
}