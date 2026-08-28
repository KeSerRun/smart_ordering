package com.smartordering.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Role mapper
 *
 * @author smartordering
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * Query roles by user ID
     */
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 物理删除角色（绕过逻辑删除）。
     *
     * <p>角色表 code 有唯一索引 {@code uk_code}，而逻辑删除只置 {@code deleted=1}，
     * 已删行仍占用唯一索引 → 同名角色无法重建。
     * 角色无业务历史语义（关联的 user_role/role_menu/role_module 删除时一并清理），
     * 因此删除角色走物理删除。</p>
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM sys_role WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}