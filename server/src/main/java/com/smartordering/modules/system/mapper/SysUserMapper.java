package com.smartordering.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * User mapper
 *
 * @author smartordering
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * Query role codes by user ID
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
         * Query permission codes by user ID
         */
        List<String> selectPermissionsByUserId(@Param("userId") Long userId);

        /**
         * 物理删除用户（绕过逻辑删除）。
         *
         * <p>同 {@code sys_role}：username 唯一索引 {@code uk_username} 会被逻辑删除行占用，
         * 导致同名账户无法重建。管理端账户删除时关联（user_role）一并清理，故物理删除。</p>
         */
        @org.apache.ibatis.annotations.Delete("DELETE FROM sys_user WHERE id = #{id}")
        int physicalDeleteById(@Param("id") Long id);
    }