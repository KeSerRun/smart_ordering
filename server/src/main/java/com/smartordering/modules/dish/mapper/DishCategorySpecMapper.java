package com.smartordering.modules.dish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.dish.entity.DishCategorySpec;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Dish category-spec binding mapper.
 *
 * @author smartordering
 */
@Mapper
public interface DishCategorySpecMapper extends BaseMapper<DishCategorySpec> {

    /**
     * 物理删除某分类的全部规格绑定。
     * <p>必须用物理删除：该表存在唯一索引 {@code uk_category_spec (category_id, spec_group_id)}，
     * 若走 @TableLogic 逻辑删除，被删行仍占着唯一键，紧接着的重建绑定会触发 DuplicateKeyException
     * （典型症状：编辑带规格绑定的分类时保存失败，事务回滚）。纯关联表无审计价值，物理删除即可。</p>
     */
    @Delete("DELETE FROM dish_category_spec WHERE category_id = #{categoryId}")
    int hardDeleteByCategoryId(@Param("categoryId") Long categoryId);
}