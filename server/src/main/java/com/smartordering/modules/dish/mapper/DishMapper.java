package com.smartordering.modules.dish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.dish.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

/**
 * Dish mapper
 *
 * @author smartordering
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}