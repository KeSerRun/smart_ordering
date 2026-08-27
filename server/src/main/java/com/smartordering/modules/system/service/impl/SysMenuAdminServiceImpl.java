package com.smartordering.modules.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.modules.system.dto.MenuCreateDTO;
import com.smartordering.modules.system.dto.MenuUpdateDTO;
import com.smartordering.modules.system.entity.SysMenu;
import com.smartordering.modules.system.entity.SysRoleMenu;
import com.smartordering.modules.system.entity.SysUserRole;
import com.smartordering.modules.system.mapper.SysMenuMapper;
import com.smartordering.modules.system.mapper.SysRoleMenuMapper;
import com.smartordering.modules.system.mapper.SysUserMapper;
import com.smartordering.modules.system.mapper.SysUserRoleMapper;
import com.smartordering.modules.system.service.SysMenuAdminService;
import com.smartordering.modules.system.vo.MenuTreeVO;
import com.smartordering.modules.system.vo.MenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin menu management service implementation.
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
public class SysMenuAdminServiceImpl implements SysMenuAdminService {

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public Long createMenu(MenuCreateDTO dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new BusinessException("Menu name is required");
        }
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        menu.setId(null);
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
        if (menu.getOrderNum() == null) {
            menu.setOrderNum(0);
        }
        menuMapper.insert(menu);
        return menu.getId();
    }

    @Override
    public void updateMenu(MenuUpdateDTO dto) {
        if (dto.getId() == null || menuMapper.selectById(dto.getId()) == null) {
            throw new BusinessException("Menu not found");
        }
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        menuMapper.updateById(menu);
    }

    @Override
    @Transactional
    public void deleteMenu(Long menuId) {
        if (menuMapper.selectById(menuId) == null) {
            throw new BusinessException("Menu not found");
        }
        // delete children recursively (simple: direct children only)
        menuMapper.delete(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, menuId));
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getMenuId, menuId));
        menuMapper.deleteById(menuId);
    }

    @Override
    public List<MenuVO> listAll() {
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                        .orderByAsc(SysMenu::getOrderNum).orderByAsc(SysMenu::getId))
                .stream().map(m -> {
                    MenuVO vo = new MenuVO();
                    BeanUtils.copyProperties(m, vo);
                    return vo;
                }).collect(Collectors.toList());
    }

    @Override
    public List<MenuTreeVO> getMenuTree() {
        return buildTree(menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getOrderNum).orderByAsc(SysMenu::getId)));
    }

    @Override
    public List<MenuTreeVO> getPermissionTree() {
        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getOrderNum).orderByAsc(SysMenu::getId));
        return buildTree(menus);
    }

    @Override
    public List<MenuTreeVO> getCurrentUserMenuTree() {
        List<SysMenu> all = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getOrderNum).orderByAsc(SysMenu::getId));
        // Super admin (role code ADMIN) sees everything; others filtered by their role menus
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> roleCodes = sysUserMapper.selectRoleCodesByUserId(userId);
        if (roleCodes.contains("ADMIN")) {
            return buildTree(all);
        }
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> menuIds = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, roleIds))
                .stream().map(SysRoleMenu::getMenuId).distinct().collect(Collectors.toList());
        List<SysMenu> myMenus = all.stream()
                .filter(m -> menuIds.contains(m.getId())).collect(Collectors.toList());
        return buildTree(myMenus);
    }

    private List<MenuTreeVO> buildTree(List<SysMenu> menus) {
        List<MenuTreeVO> nodes = menus.stream().map(m -> {
            MenuTreeVO vo = new MenuTreeVO();
            BeanUtils.copyProperties(m, vo);
            return vo;
        }).collect(Collectors.toList());
        List<MenuTreeVO> roots = new ArrayList<>();
        for (MenuTreeVO node : nodes) {
            boolean isRoot = node.getParentId() == null || node.getParentId() == 0
                    || nodes.stream().noneMatch(p -> p.getId().equals(node.getParentId()));
            if (isRoot) {
                roots.add(node);
            } else {
                nodes.stream().filter(p -> p.getId().equals(node.getParentId()))
                        .findFirst().ifPresent(parent -> {
                            if (parent.getChildren() == null) {
                                parent.setChildren(new ArrayList<>());
                            }
                            parent.getChildren().add(node);
                        });
            }
        }
        return roots;
    }
}