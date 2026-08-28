package com.smartordering.modules.banner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.common.result.PageResult;
import com.smartordering.modules.banner.dto.HomeBannerCreateDTO;
import com.smartordering.modules.banner.dto.HomeBannerQueryDTO;
import com.smartordering.modules.banner.dto.HomeBannerUpdateDTO;
import com.smartordering.modules.banner.entity.HomeBanner;
import com.smartordering.modules.banner.mapper.HomeBannerMapper;
import com.smartordering.modules.banner.service.HomeBannerService;
import com.smartordering.modules.banner.vo.HomeBannerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Home banner service implementation.
 *
 * @author smartordering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeBannerServiceImpl implements HomeBannerService {

    private final HomeBannerMapper homeBannerMapper;

    @Override
    public List<HomeBannerVO> listEnabled(String scene) {
        LambdaQueryWrapper<HomeBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeBanner::getStatus, 1);
        if (scene != null && !scene.isEmpty()) {
            wrapper.eq(HomeBanner::getScene, scene);
        }
        wrapper.orderByAsc(HomeBanner::getSort);
        return homeBannerMapper.selectList(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<HomeBannerVO> pageList(HomeBannerQueryDTO dto) {
        long pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        LambdaQueryWrapper<HomeBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getTitle()), HomeBanner::getTitle, dto.getTitle())
                .eq(dto.getStatus() != null, HomeBanner::getStatus, dto.getStatus())
                .eq(StringUtils.hasText(dto.getScene()), HomeBanner::getScene, dto.getScene())
                .orderByAsc(HomeBanner::getSort).orderByAsc(HomeBanner::getId);
        Page<HomeBanner> page = new Page<>(pageNum, pageSize);
        homeBannerMapper.selectPage(page, wrapper);
        List<HomeBannerVO> list = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public void create(HomeBannerCreateDTO dto) {
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new BusinessException("Title is required");
        }
        HomeBanner banner = new HomeBanner();
        BeanUtils.copyProperties(dto, banner);
        banner.setId(null);
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        if (banner.getSort() == null) {
            banner.setSort(0);
        }
        homeBannerMapper.insert(banner);
    }

    @Override
    public void update(HomeBannerUpdateDTO dto) {
        if (dto.getId() == null || homeBannerMapper.selectById(dto.getId()) == null) {
            throw new BusinessException("Banner not found");
        }
        HomeBanner banner = new HomeBanner();
        BeanUtils.copyProperties(dto, banner);
        homeBannerMapper.updateById(banner);
    }

    @Override
        public void updateStatus(Long id, Integer status) {
            HomeBanner banner = homeBannerMapper.selectById(id);
            if (banner == null) {
                throw new BusinessException("Banner not found");
            }
            HomeBanner update = new HomeBanner();
            update.setId(id);
            update.setStatus(status);
            homeBannerMapper.updateById(update);
        }

        @Override
        public void delete(Long id) {
            HomeBanner banner = homeBannerMapper.selectById(id);
            if (banner == null) {
                throw new BusinessException("Banner not found");
            }
            homeBannerMapper.deleteById(id);
            log.info("Home banner deleted: id={}, title={}", id, banner.getTitle());
        }

    private HomeBannerVO toVO(HomeBanner banner) {
        HomeBannerVO vo = new HomeBannerVO();
        BeanUtils.copyProperties(banner, vo);
        return vo;
    }
}