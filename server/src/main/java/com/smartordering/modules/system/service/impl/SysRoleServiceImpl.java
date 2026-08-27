package com.smartordering.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.common.result.PageResult;
import com.smartordering.modules.system.dto.RoleCreateDTO;
import com.smartordering.modules.system.dto.RoleQueryDTO;
import com.smartordering.modules.system.dto.RoleUpdateDTO;
import com.smartordering.modules.system.entity.SysRole;
import com.smartordering.modules.system.entity.SysRoleMenu;
import com.smartordering.modules.system.entity.SysUserRole;
import com.smartordering.modules.system.mapper.SysRoleMapper;
import com.smartordering.modules.system.mapper.SysRoleMenuMapper;
import com.smartordering.modules.system.mapper.SysUserRoleMapper;
import com.smartordering.modules.system.service.SysRoleService;
import com.smartordering.modules.system.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Role service implementation.
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    @Transactional
    public Long createRole(RoleCreateDTO dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new BusinessException("Role name is required");
        }
        if (StringUtils.hasText(dto.getCode())
                && roleMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, dto.getCode())) > 0) {
            throw new BusinessException("Role code already exists");
        }
        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        role.setId(null);
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        roleMapper.insert(role);
        return role.getId();
    }

    @Override
    @Transactional
    public void updateRole(RoleUpdateDTO dto) {
        if (dto.getId() == null || roleMapper.selectById(dto.getId()) == null) {
            throw new BusinessException("Role not found");
        }
        SysRole role = new SysRole();
        role.setId(dto.getId());
        if (StringUtils.hasText(dto.getName())) {
            role.setName(dto.getName());
        }
        if (StringUtils.hasText(dto.getCode())) {
            role.setCode(dto.getCode());
        }
        if (dto.getStatus() != null) {
            role.setStatus(dto.getStatus());
        }
        if (dto.getRemark() != null) {
            role.setRemark(dto.getRemark());
        }
        roleMapper.updateById(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        if (roleMapper.selectById(roleId) == null) {
            throw new BusinessException("Role not found");
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
        roleMapper.deleteById(roleId);
    }

    @Override
    public PageResult<RoleVO> pageList(RoleQueryDTO dto) {
        long pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getName()), SysRole::getName, dto.getName())
                .like(StringUtils.hasText(dto.getCode()), SysRole::getCode, dto.getCode())
                .eq(dto.getStatus() != null, SysRole::getStatus, dto.getStatus())
                .orderByAsc(SysRole::getId);
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        roleMapper.selectPage(page, wrapper);
        List<RoleVO> list = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public List<RoleVO> listAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public RoleVO getInfo(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("Role not found");
        }
        return toVO(role);
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Long menuId : menuIds.stream().distinct().collect(Collectors.toList())) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    @Override
    public void updateStatus(Long roleId, Integer status) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("Role not found");
        }
        SysRole update = new SysRole();
        update.setId(roleId);
        update.setStatus(status);
        roleMapper.updateById(update);
    }

    @Override
    @Transactional
    public void assignUsers(Long roleId, List<Long> userIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds.stream().distinct().collect(Collectors.toList())) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    @Override
    public List<Long> getRoleUserIds(Long roleId) {
        return userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId))
                .stream().map(SysUserRole::getUserId).collect(Collectors.toList());
    }

    private RoleVO toVO(SysRole role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }
}