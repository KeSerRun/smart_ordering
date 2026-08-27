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
}