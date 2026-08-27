package com.smartordering.modules.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.common.result.PageResult;
import com.smartordering.modules.system.dto.AdminResetPasswordDTO;
import com.smartordering.modules.system.dto.LoginDTO;
import com.smartordering.modules.system.dto.PasswordUpdateDTO;
import com.smartordering.modules.system.dto.RegisterDTO;
import com.smartordering.modules.system.dto.UserQueryDTO;
import com.smartordering.modules.system.dto.UserUpdateDTO;
import com.smartordering.modules.system.entity.SysUser;
import com.smartordering.modules.system.mapper.SysUserMapper;
import com.smartordering.modules.system.service.SysUserService;
import com.smartordering.modules.system.vo.LoginVO;
import com.smartordering.modules.system.vo.UserInfoVO;
import com.smartordering.modules.system.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
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

    private final SysUserMapper sysUserMapper;
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
        return vo;
    }
}