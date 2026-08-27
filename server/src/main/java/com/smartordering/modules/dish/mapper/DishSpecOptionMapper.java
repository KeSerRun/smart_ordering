package com.smartordering.modules.dish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.dish.entity.DishSpecOption;
import org.apache.ibatis.annotations.Mapper;

/**
 * Dish spec option mapper.
 *
 * @author smartordering
 */
@Mapper
public interface DishSpecOptionMapper extends BaseMapper<DishSpecOption> {
}