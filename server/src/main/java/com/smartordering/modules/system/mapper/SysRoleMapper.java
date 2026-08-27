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
}