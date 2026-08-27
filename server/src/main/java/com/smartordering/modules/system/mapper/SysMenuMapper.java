package com.smartordering.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * System menu mapper
 *
 * @author smartordering
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * Query menus authorized to a user (user -> role -> menu join)
     */
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
}