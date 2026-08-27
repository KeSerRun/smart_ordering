package com.smartordering.modules.dish.service;

import com.smartordering.common.result.PageVO;
import com.smartordering.modules.dish.dto.DishAdminQueryDTO;
import com.smartordering.modules.dish.dto.DishCreateDTO;
import com.smartordering.modules.dish.vo.AdminDishVO;
import com.smartordering.modules.dish.vo.DishVO;

import java.util.List;

/**
 * Dish service interface.
 *
 * @author smartordering
 */
public interface DishService {

    /**
     * List on-sale dishes (optionally filtered by category)
     */
    List<DishVO> listOnSale(Long categoryId);

    /**
     * Get dish detail by ID (app side)
     */
    DishVO getDetail(Long id);

    /**
     * Paged list for the admin side (with category name + spec items)
     */
    PageVO<AdminDishVO> pageQuery(DishAdminQueryDTO query);

    /**
     * Get dish detail for the admin side
     */
    AdminDishVO getAdminDetail(Long id);

    /**
     * Create a dish, returns the new dish id
     */
    Long createDish(DishCreateDTO dto);

    /**
     * Update a dish by id
     */
    void updateDish(Long id, DishCreateDTO dto);

    /**
     * Update on/off shelf status (0=off, 1=on)
     */
    void updateDishStatus(Long id, Integer status);

    /**
     * Update sold-out flag (0=normal, 1=sold out)
     */
    void updateDishSoldOut(Long id, Integer soldOut);
}