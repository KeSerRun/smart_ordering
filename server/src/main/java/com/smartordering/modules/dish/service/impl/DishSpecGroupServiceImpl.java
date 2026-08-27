package com.smartordering.modules.dish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.modules.dish.dto.DishSpecGroupDTO;
import com.smartordering.modules.dish.entity.DishCategorySpec;
import com.smartordering.modules.dish.entity.DishSpecGroup;
import com.smartordering.modules.dish.entity.DishSpecOption;
import com.smartordering.modules.dish.mapper.DishCategorySpecMapper;
import com.smartordering.modules.dish.mapper.DishSpecGroupMapper;
import com.smartordering.modules.dish.mapper.DishSpecOptionMapper;
import com.smartordering.modules.dish.service.DishSpecGroupService;
import com.smartordering.modules.dish.vo.DishSpecGroupVO;
import com.smartordering.modules.dish.vo.DishSpecOptionVO;
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
 * Dish spec group service implementation.
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
public class DishSpecGroupServiceImpl implements DishSpecGroupService {

    private final DishSpecGroupMapper specGroupMapper;
    private final DishSpecOptionMapper specOptionMapper;
    private final DishCategorySpecMapper categorySpecMapper;

    @Override
    public List<DishSpecGroupVO> listGroups() {
        LambdaQueryWrapper<DishSpecGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(DishSpecGroup::getSort).orderByAsc(DishSpecGroup::getId);
        List<DishSpecGroup> groups = specGroupMapper.selectList(wrapper);

        // groupId -> options
        Map<Long, List<DishSpecOption>> optionsByGroup = specOptionMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(DishSpecOption::getGroupId));

        return groups.stream().map(group -> {
            DishSpecGroupVO vo = new DishSpecGroupVO();
            BeanUtils.copyProperties(group, vo);
            List<DishSpecOptionVO> options = optionsByGroup.getOrDefault(group.getId(), Collections.emptyList())
                    .stream()
                    .sorted((a, b) -> Integer.compare(
                            a.getSort() == null ? 0 : a.getSort(),
                            b.getSort() == null ? 0 : b.getSort()))
                    .map(option -> {
                        DishSpecOptionVO optionVO = new DishSpecOptionVO();
                        BeanUtils.copyProperties(option, optionVO);
                        return optionVO;
                    })
                    .collect(Collectors.toList());
            vo.setOptions(options);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long createGroup(DishSpecGroupDTO dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new BusinessException("Spec group name is required");
        }
        DishSpecGroup group = new DishSpecGroup();
        BeanUtils.copyProperties(dto, group);
        group.setId(null);
        if (group.getStatus() == null) {
            group.setStatus(1);
        }
        if (group.getSort() == null) {
            group.setSort(0);
        }
        specGroupMapper.insert(group);
        replaceOptions(group.getId(), dto.getOptions());
        return group.getId();
    }

    @Override
    @Transactional
    public void updateGroup(DishSpecGroupDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("Spec group id is required");
        }
        if (specGroupMapper.selectById(dto.getId()) == null) {
            throw new BusinessException("Spec group not found");
        }
        DishSpecGroup group = new DishSpecGroup();
        group.setId(dto.getId());
        if (StringUtils.hasText(dto.getName())) {
            group.setName(dto.getName());
        }
        if (dto.getSort() != null) {
            group.setSort(dto.getSort());
        }
        if (dto.getStatus() != null) {
            group.setStatus(dto.getStatus());
        }
        specGroupMapper.updateById(group);
        if (dto.getOptions() != null) {
            replaceOptions(dto.getId(), dto.getOptions());
        }
    }

    @Override
    @Transactional
    public void deleteGroup(Long id) {
        if (specGroupMapper.selectById(id) == null) {
            throw new BusinessException("Spec group not found");
        }
        deleteOptions(id);
        LambdaQueryWrapper<DishCategorySpec> bindingWrapper = new LambdaQueryWrapper<>();
        bindingWrapper.eq(DishCategorySpec::getSpecGroupId, id);
        categorySpecMapper.delete(bindingWrapper);
        specGroupMapper.deleteById(id);
    }

    // ==================== helpers ====================

    private void replaceOptions(Long groupId, List<DishSpecGroupDTO.OptionDTO> options) {
        deleteOptions(groupId);
        if (options == null || options.isEmpty()) {
            return;
        }
        for (DishSpecGroupDTO.OptionDTO optionDTO : options) {
            if (!StringUtils.hasText(optionDTO.getName())) {
                continue;
            }
            DishSpecOption option = new DishSpecOption();
            option.setGroupId(groupId);
            option.setName(optionDTO.getName());
            option.setSort(optionDTO.getSort() == null ? 0 : optionDTO.getSort());
            specOptionMapper.insert(option);
        }
    }

    private void deleteOptions(Long groupId) {
        LambdaQueryWrapper<DishSpecOption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishSpecOption::getGroupId, groupId);
        specOptionMapper.delete(wrapper);
    }
}