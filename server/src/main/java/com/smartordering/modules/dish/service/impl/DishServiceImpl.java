package com.smartordering.modules.dish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.common.result.PageVO;
import com.smartordering.framework.config.MinioConfig;
import com.smartordering.modules.dish.dto.DishAdminQueryDTO;
import com.smartordering.modules.dish.dto.DishCreateDTO;
import com.smartordering.modules.dish.dto.DishSpecItemDTO;
import com.smartordering.modules.dish.entity.Dish;
import com.smartordering.modules.dish.entity.DishCategory;
import com.smartordering.modules.dish.mapper.DishCategoryMapper;
import com.smartordering.modules.dish.mapper.DishMapper;
import com.smartordering.modules.dish.service.DishService;
import com.smartordering.modules.dish.vo.AdminDishVO;
import com.smartordering.modules.dish.vo.DishSpecItemVO;
import com.smartordering.modules.dish.vo.DishVO;
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
 * Dish service implementation.
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final DishCategoryMapper dishCategoryMapper;
    private final ObjectMapper objectMapper;
    private final MinioConfig minioConfig;

    @Override
    public List<DishVO> listOnSale(Long categoryId) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStatus, 1)
               .eq(Dish::getSoldOut, 0);
        if (categoryId != null) {
            wrapper.eq(Dish::getCategoryId, categoryId);
        }
        wrapper.orderByAsc(Dish::getCategoryId).orderByAsc(Dish::getId);

        List<Dish> dishes = dishMapper.selectList(wrapper);
        return dishes.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public DishVO getDetail(Long id) {
        Dish dish = requireDish(id);
        return toVO(dish);
    }

    @Override
    public PageVO<AdminDishVO> pageQuery(DishAdminQueryDTO query) {
        int pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();

        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        if (query.getCategoryId() != null) {
            wrapper.eq(Dish::getCategoryId, query.getCategoryId());
        }
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(Dish::getName, query.getName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Dish::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Dish::getCreateTime).orderByDesc(Dish::getId);

        Page<Dish> page = new Page<>(pageNum, pageSize);
        IPage<Dish> result = dishMapper.selectPage(page, wrapper);
        Map<Long, String> categoryNames = loadCategoryNameMap();

        List<AdminDishVO> list = result.getRecords().stream()
                .map(dish -> toAdminVO(dish, categoryNames))
                .collect(Collectors.toList());
        return new PageVO<>(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public AdminDishVO getAdminDetail(Long id) {
        return toAdminVO(requireDish(id), loadCategoryNameMap());
    }

    @Override
    @Transactional
    public Long createDish(DishCreateDTO dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new BusinessException("Dish name is required");
        }
        if (dto.getPrice() == null) {
            throw new BusinessException("Dish price is required");
        }
        Dish dish = new Dish();
        BeanUtils.copyProperties(dto, dish);
        dish.setId(null);
        dish.setSpecValues(serializeSpecItems(dto.getSpecItems()));
        if (dish.getStatus() == null) {
            dish.setStatus(1);
        }
        if (dish.getSoldOut() == null) {
            dish.setSoldOut(0);
        }
        if (dish.getStock() == null) {
            dish.setStock(-1);
        }
        dishMapper.insert(dish);
        return dish.getId();
    }

    @Override
    @Transactional
    public void updateDish(Long id, DishCreateDTO dto) {
        requireDish(id);
        // Only non-null fields are written by updateById (MyBatis-Plus NOT_NULL strategy),
        // so partial updates from the admin form work as expected.
        Dish dish = new Dish();
        dish.setId(id);
        if (dto.getCategoryId() != null) {
            dish.setCategoryId(dto.getCategoryId());
        }
        if (StringUtils.hasText(dto.getName())) {
            dish.setName(dto.getName());
        }
        if (dto.getPrice() != null) {
            dish.setPrice(dto.getPrice());
        }
        if (dto.getImage() != null) {
            dish.setImage(dto.getImage());
        }
        if (dto.getThumbnail() != null) {
            dish.setThumbnail(dto.getThumbnail());
        }
        if (dto.getSpiceLevel() != null) {
            dish.setSpiceLevel(dto.getSpiceLevel());
        }
        if (dto.getIngredients() != null) {
            dish.setIngredients(dto.getIngredients());
        }
        if (dto.getDescription() != null) {
            dish.setDescription(dto.getDescription());
        }
        if (dto.getStock() != null) {
            dish.setStock(dto.getStock());
        }
        if (dto.getPreparationTime() != null) {
            dish.setPreparationTime(dto.getPreparationTime());
        }
        if (dto.getSpecItems() != null && !dto.getSpecItems().isEmpty()) {
            dish.setSpecValues(serializeSpecItems(dto.getSpecItems()));
        }
        dishMapper.updateById(dish);
    }

    @Override
    @Transactional
    public void updateDishStatus(Long id, Integer status) {
        Dish dish = requireDish(id);
        dish.setStatus(status == null ? 0 : status);
        dishMapper.updateById(dish);
    }

    @Override
    @Transactional
    public void updateDishSoldOut(Long id, Integer soldOut) {
        Dish dish = requireDish(id);
        dish.setSoldOut(soldOut == null ? 0 : soldOut);
        dishMapper.updateById(dish);
    }

    // ==================== helpers ====================

    private Dish requireDish(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException("Dish not found");
        }
        return dish;
    }

    private Map<Long, String> loadCategoryNameMap() {
        return dishCategoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(DishCategory::getId, DishCategory::getName));
    }

    private AdminDishVO toAdminVO(Dish dish, Map<Long, String> categoryNames) {
        AdminDishVO vo = new AdminDishVO();
        BeanUtils.copyProperties(dish, vo);
        vo.setImage(resolveImageUrl(dish.getImage()));
        vo.setThumbnail(resolveImageUrl(dish.getThumbnail()));
        vo.setCategoryName(categoryNames.get(dish.getCategoryId()));
        vo.setSpecItems(deserializeSpecItems(dish.getSpecValues()));
        return vo;
    }

    private DishVO toVO(Dish dish) {
        DishVO vo = new DishVO();
        BeanUtils.copyProperties(dish, vo);
        vo.setImage(resolveImageUrl(dish.getImage()));
        vo.setThumbnail(resolveImageUrl(dish.getThumbnail()));
        return vo;
    }

    /**
     * Resolve a stored image value into a full URL.
     * <p>Values that already carry a scheme (http/https) are returned unchanged;
     * otherwise the value is treated as an object key inside the MinIO bucket and
     * prefixed with the bucket's public base URL.</p>
     */
    private String resolveImageUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucket() + "/" + value;
    }

    private String serializeSpecItems(List<DishSpecItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(items);
        } catch (Exception e) {
            throw new BusinessException("Invalid spec items");
        }
    }

    private List<DishSpecItemVO> deserializeSpecItems(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<DishSpecItemVO>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}