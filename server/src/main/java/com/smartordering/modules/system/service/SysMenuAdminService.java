package com.smartordering.modules.system.service;

import com.smartordering.modules.system.dto.MenuCreateDTO;
import com.smartordering.modules.system.dto.MenuUpdateDTO;
import com.smartordering.modules.system.vo.MenuTreeVO;
import com.smartordering.modules.system.vo.MenuVO;

import java.util.List;

/**
 * Admin menu management service interface (separate from route-building service).
 *
 * @author smartordering
 */
public interface SysMenuAdminService {

    Long createMenu(MenuCreateDTO dto);

    void updateMenu(MenuUpdateDTO dto);

    void deleteMenu(Long menuId);

    List<MenuVO> listAll();

    List<MenuTreeVO> getMenuTree();

    List<MenuTreeVO> getPermissionTree();

    List<MenuTreeVO> getCurrentUserMenuTree();
}