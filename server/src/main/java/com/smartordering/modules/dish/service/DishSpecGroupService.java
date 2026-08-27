package com.smartordering.modules.dish.service;

import com.smartordering.modules.dish.dto.DishSpecGroupDTO;
import com.smartordering.modules.dish.vo.DishSpecGroupVO;

import java.util.List;

/**
 * Dish spec group service interface.
 *
 * @author smartordering
 */
public interface DishSpecGroupService {

    /**
     * List all spec groups with their options
     */
    List<DishSpecGroupVO> listGroups();

    /**
     * Create a spec group, returns the new id
     */
    Long createGroup(DishSpecGroupDTO dto);

    /**
     * Update a spec group, replacing its options
     */
    void updateGroup(DishSpecGroupDTO dto);

    /**
     * Delete a spec group, its options and category bindings
     */
    void deleteGroup(Long id);
}