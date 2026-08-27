package com.smartordering.modules.dish.service;

import com.smartordering.modules.dish.dto.DishCategoryAdminDTO;
import com.smartordering.modules.dish.dto.DishCategorySortDTO;
import com.smartordering.modules.dish.vo.AdminDishCategoryVO;
import com.smartordering.modules.dish.vo.DishCategoryVO;

import java.util.List;

/**
 * Dish category service interface.
 *
 * @author smartordering
 */
public interface DishCategoryService {

    /**
     * List enabled categories (sorted by sort asc), app side
     */
    List<DishCategoryVO> listEnabled();

    /**
     * List all categories with bound spec groups, admin side
     */
    List<AdminDishCategoryVO> listAdmin();

    /**
     * Create a category, returns the new id
     */
    Long createCategory(DishCategoryAdminDTO dto);

    /**
     * Update a category
     */
    void updateCategory(DishCategoryAdminDTO dto);

    /**
     * Delete a category (blocked when dishes reference it)
     */
    void deleteCategory(Long id);

    /**
     * Batch update category sort order
     */
    void updateSort(DishCategorySortDTO dto);
}