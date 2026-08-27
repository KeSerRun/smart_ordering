package com.smartordering.modules.system.service;

import com.smartordering.common.result.PageResult;
import com.smartordering.modules.system.dto.RoleCreateDTO;
import com.smartordering.modules.system.dto.RoleQueryDTO;
import com.smartordering.modules.system.dto.RoleUpdateDTO;
import com.smartordering.modules.system.vo.RoleVO;

import java.util.List;

/**
 * Role service interface.
 *
 * @author smartordering
 */
public interface SysRoleService {

    Long createRole(RoleCreateDTO dto);

    void updateRole(RoleUpdateDTO dto);

    void deleteRole(Long roleId);

    PageResult<RoleVO> pageList(RoleQueryDTO dto);

    List<RoleVO> listAll();

    RoleVO getInfo(Long roleId);

    void assignMenus(Long roleId, List<Long> menuIds);

    List<Long> getRoleMenuIds(Long roleId);

    void updateStatus(Long roleId, Integer status);

    void assignUsers(Long roleId, List<Long> userIds);

    List<Long> getRoleUserIds(Long roleId);
}