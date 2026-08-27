package com.smartordering.modules.dish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.dish.entity.DishCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * Dish category mapper
 *
 * @author smartordering
 */
@Mapper
public interface DishCategoryMapper extends BaseMapper<DishCategory> {
}