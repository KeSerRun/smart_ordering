package com.smartordering.modules.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
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
import com.smartordering.modules.system.entity.SysUser;
import com.smartordering.modules.system.entity.SysUserModule;
import com.smartordering.modules.system.mapper.SysUserMapper;
import com.smartordering.modules.system.mapper.SysUserModuleMapper;
import com.smartordering.modules.system.service.SysUserService;
import com.smartordering.modules.system.vo.LoginVO;
import com.smartordering.modules.system.vo.UserInfoVO;
import com.smartordering.modules.system.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User service implementation.
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    /** 全部模块编码（与前端侧边栏分组一致） */
    public static final List<String> ALL_MODULES = List.of("core", "ops", "sys", "kitchen");

    /** 内置超级管理员用户名：不可删除、不可禁用 */
    private static final String SUPER_ADMIN = "admin";

    private final SysUserMapper sysUserMapper;
    private final SysUserModuleMapper sysUserModuleMapper;
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
    public LoginVO login(LoginDTO dto) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        SysUser user = sysUserMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("User not found");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("Account is disabled");
        }
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid password");
        }

        StpUtil.login(user.getId());

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
                List<UserVO> list = page.getRecords().stream().map(u -> {
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(u, vo);
                    vo.setModules(getUserModules(u.getId()));
                    return vo;
                }).collect(Collectors.toList());
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
        saveUserModules(user.getId(), dto.getModules());
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
        if (dto.getModules() != null) {
            saveUserModules(dto.getId(), dto.getModules());
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
        // 逻辑删除用户 + 清理关联：用户-模块
        sysUserMapper.deleteById(userId);
        sysUserModuleMapper.delete(new LambdaQueryWrapper<SysUserModule>()
                .eq(SysUserModule::getUserId, userId));
        StpUtil.kickout(userId);
        permissionCacheService.clearUserCache(userId);
    }

    @Override
    @Transactional
    public void updateUserModules(Long userId, List<String> modules) {
        requireUser(userId);
        saveUserModules(userId, modules);
        permissionCacheService.clearUserCache(userId);
    }

    // ==================== helpers ====================

    private SysUser requireUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }
        return user;
    }

    /** 合法性校验 + 去重，未知模块编码忽略 */
    private List<String> normalizeModules(List<String> modules) {
        if (modules == null || modules.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> valid = new HashSet<>(ALL_MODULES);
        return modules.stream().distinct().filter(valid::contains).collect(Collectors.toList());
    }

    private List<String> getUserModules(Long userId) {
        return sysUserModuleMapper.selectList(new LambdaQueryWrapper<SysUserModule>()
                        .eq(SysUserModule::getUserId, userId))
                .stream().map(SysUserModule::getModuleCode).collect(Collectors.toList());
    }

    /** 全量替换用户模块权限（先删后插） */
    private void saveUserModules(Long userId, List<String> modules) {
        List<String> normalized = normalizeModules(modules);
        sysUserModuleMapper.delete(new LambdaQueryWrapper<SysUserModule>()
                .eq(SysUserModule::getUserId, userId));
        if (normalized.isEmpty()) {
            return;
        }
        for (String code : normalized) {
            SysUserModule row = new SysUserModule();
            row.setUserId(userId);
            row.setModuleCode(code);
            sysUserModuleMapper.insert(row);
        }
    }
}