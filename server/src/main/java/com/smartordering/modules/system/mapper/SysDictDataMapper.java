package com.smartordering.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.system.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 字典数据 Mapper
 *

 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    /**
     * 根据字典类型ID删除数据
     *
     * @param typeId 字典类型ID
     * @return 删除数量
     */
    int deleteByTypeId(@Param("typeId") Long typeId);
}
