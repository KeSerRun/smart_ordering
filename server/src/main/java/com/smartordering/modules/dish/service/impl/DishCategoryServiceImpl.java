package com.smartordering.modules.dish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.modules.dish.dto.DishCategoryAdminDTO;
import com.smartordering.modules.dish.dto.DishCategorySortDTO;
import com.smartordering.modules.dish.entity.Dish;
import com.smartordering.modules.dish.entity.DishCategory;
import com.smartordering.modules.dish.entity.DishCategorySpec;
import com.smartordering.modules.dish.entity.DishSpecGroup;
import com.smartordering.modules.dish.mapper.DishCategoryMapper;
import com.smartordering.modules.dish.mapper.DishCategorySpecMapper;
import com.smartordering.modules.dish.mapper.DishMapper;
import com.smartordering.modules.dish.mapper.DishSpecGroupMapper;
import com.smartordering.modules.dish.service.DishCategoryService;
import com.smartordering.modules.dish.vo.AdminDishCategoryVO;
import com.smartordering.modules.dish.vo.DishCategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dish category service implementation.
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
public class DishCategoryServiceImpl implements DishCategoryService {

    private final DishCategoryMapper dishCategoryMapper;
    private final DishCategorySpecMapper categorySpecMapper;
    private final DishSpecGroupMapper specGroupMapper;
    private final DishMapper dishMapper;

    @Override
    public List<DishCategoryVO> listEnabled() {
        LambdaQueryWrapper<DishCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishCategory::getStatus, 1)
               .orderByAsc(DishCategory::getSort);

        List<DishCategory> list = dishCategoryMapper.selectList(wrapper);
        return list.stream().map(c -> {
            DishCategoryVO vo = new DishCategoryVO();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<AdminDishCategoryVO> listAdmin() {
        LambdaQueryWrapper<DishCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(DishCategory::getSort).orderByAsc(DishCategory::getId);
        List<DishCategory> categories = dishCategoryMapper.selectList(wrapper);

        // categoryId -> [specGroupId]
        Map<Long, List<Long>> specsByCategory = categorySpecMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(
                        DishCategorySpec::getCategoryId,
                        Collectors.mapping(DishCategorySpec::getSpecGroupId, Collectors.toList())));

        // specGroupId -> name
        Map<Long, String> groupNames = specGroupMapper.selectList(null).stream()
                .collect(Collectors.toMap(DishSpecGroup::getId, DishSpecGroup::getName));

        return categories.stream().map(c -> {
            List<Long> specGroupIds = specsByCategory.getOrDefault(c.getId(), Collections.emptyList());
            AdminDishCategoryVO vo = new AdminDishCategoryVO();
            BeanUtils.copyProperties(c, vo);
            vo.setSpecGroupIds(specGroupIds);
            vo.setSpecGroupNames(specGroupIds.stream().map(id -> groupNames.get(id)).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long createCategory(DishCategoryAdminDTO dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new BusinessException("Category name is required");
        }
        DishCategory category = new DishCategory();
        BeanUtils.copyProperties(dto, category);
        category.setId(null);
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        dishCategoryMapper.insert(category);
        replaceBindings(category.getId(), dto.getSpecGroupIds());
        return category.getId();
    }

    @Override
    @Transactional
    public void updateCategory(DishCategoryAdminDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("Category id is required");
        }
        if (dishCategoryMapper.selectById(dto.getId()) == null) {
            throw new BusinessException("Category not found");
        }
        DishCategory category = new DishCategory();
        category.setId(dto.getId());
        if (StringUtils.hasText(dto.getName())) {
            category.setName(dto.getName());
        }
        if (dto.getSort() != null) {
            category.setSort(dto.getSort());
        }
        if (dto.getStatus() != null) {
            category.setStatus(dto.getStatus());
        }
        if (dto.getImage() != null) {
            category.setImage(dto.getImage());
        }
        dishCategoryMapper.updateById(category);
        if (dto.getSpecGroupIds() != null) {
            replaceBindings(dto.getId(), dto.getSpecGroupIds());
        }
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (dishCategoryMapper.selectById(id) == null) {
            throw new BusinessException("Category not found");
        }
        Long count = dishMapper.selectCount(
                new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, id));
        if (count != null && count > 0) {
            throw new BusinessException("Category has dishes, cannot delete");
        }
        deleteBindings(id);
        dishCategoryMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateSort(DishCategorySortDTO dto) {
        if (dto.getItems() == null) {
            return;
        }
        for (DishCategorySortDTO.SortItem item : dto.getItems()) {
            if (item.getId() == null) {
                continue;
            }
            DishCategory category = new DishCategory();
            category.setId(item.getId());
            category.setSort(item.getSort() == null ? 0 : item.getSort());
            dishCategoryMapper.updateById(category);
        }
    }

    // ==================== helpers ====================

    private void replaceBindings(Long categoryId, List<Long> specGroupIds) {
        deleteBindings(categoryId);
        if (specGroupIds == null || specGroupIds.isEmpty()) {
            return;
        }
        for (Long groupId : specGroupIds.stream().distinct().collect(Collectors.toList())) {
            DishCategorySpec binding = new DishCategorySpec();
            binding.setCategoryId(categoryId);
            binding.setSpecGroupId(groupId);
            categorySpecMapper.insert(binding);
        }
    }

    private void deleteBindings(Long categoryId) {
        LambdaQueryWrapper<DishCategorySpec> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishCategorySpec::getCategoryId, categoryId);
        categorySpecMapper.delete(wrapper);
    }
}