package com.smartordering.modules.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.common.result.PageResult;
import com.smartordering.modules.coupon.dto.CouponTemplateCreateDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateQueryDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateUpdateDTO;
import com.smartordering.modules.coupon.dto.UserCouponQueryDTO;
import com.smartordering.modules.coupon.entity.CouponTemplate;
import com.smartordering.modules.coupon.entity.UserCoupon;
import com.smartordering.modules.coupon.mapper.CouponTemplateMapper;
import com.smartordering.modules.coupon.mapper.UserCouponMapper;
import com.smartordering.modules.coupon.service.CouponService;
import com.smartordering.modules.coupon.vo.CouponTemplateVO;
import com.smartordering.modules.coupon.vo.UserCouponVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Coupon service implementation.
 *
 * @author smartordering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponTemplateMapper couponTemplateMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    public List<CouponTemplateVO> listAvailableTemplates() {
        LambdaQueryWrapper<CouponTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponTemplate::getStatus, 1);
        return couponTemplateMapper.selectList(wrapper).stream().map(t -> {
            CouponTemplateVO vo = new CouponTemplateVO();
            BeanUtils.copyProperties(t, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void receive(Long userId, Long templateId) {
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null || template.getStatus() != 1) {
            throw new BusinessException("Coupon template not available");
        }

        if (template.getPerUserLimit() != null && template.getPerUserLimit() > 0) {
            LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserCoupon::getTemplateId, templateId).eq(UserCoupon::getUserId, userId);
            long count = userCouponMapper.selectCount(wrapper);
            if (count >= template.getPerUserLimit()) {
                throw new BusinessException("Receive limit reached");
            }
        }

        UserCoupon uc = new UserCoupon();
        uc.setTemplateId(templateId);
        uc.setUserId(userId);
        uc.setCouponName(template.getName());
        uc.setCouponType(template.getType());
        uc.setThresholdAmount(template.getThresholdAmount());
        uc.setDiscountAmount(template.getDiscountAmount());
        uc.setDiscountRate(template.getDiscountRate());
        uc.setSourceType(1);
        uc.setStatus(0);
        uc.setReceivedTime(LocalDateTime.now());
        uc.setAvailableWeekdays(template.getAvailableWeekdays());

        if (template.getValidityType() == 1) {
            uc.setValidFrom(template.getValidFrom());
            uc.setValidTo(template.getValidTo());
        } else if (template.getValidityType() == 2) {
            uc.setValidFrom(LocalDateTime.now());
            uc.setValidTo(LocalDateTime.now().plusDays(
                    template.getValidDays() == null ? 0 : template.getValidDays()));
        }
        userCouponMapper.insert(uc);

        template.setIssuedQuantity((template.getIssuedQuantity() == null ? 0 : template.getIssuedQuantity()) + 1);
        couponTemplateMapper.updateById(template);
        log.info("Coupon received: userId={}, templateId={}", userId, templateId);
    }

    @Override
    public List<UserCouponVO> listMyCoupons(Long userId) {
        return userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .orderByDesc(UserCoupon::getReceivedTime))
                .stream().map(this::toUserCouponVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<CouponTemplateVO> pageTemplates(CouponTemplateQueryDTO dto) {
        long pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        LambdaQueryWrapper<CouponTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getName()), CouponTemplate::getName, dto.getName())
                .eq(dto.getType() != null, CouponTemplate::getType, dto.getType())
                .eq(dto.getStatus() != null, CouponTemplate::getStatus, dto.getStatus())
                .orderByDesc(CouponTemplate::getCreateTime);
        Page<CouponTemplate> page = new Page<>(pageNum, pageSize);
        couponTemplateMapper.selectPage(page, wrapper);
        List<CouponTemplateVO> list = page.getRecords().stream().map(this::toTemplateVO).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public Long createTemplate(CouponTemplateCreateDTO dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new BusinessException("Template name is required");
        }
        CouponTemplate template = new CouponTemplate();
        BeanUtils.copyProperties(dto, template);
        template.setId(null);
        if (template.getStatus() == null) {
            template.setStatus(1);
        }
        if (template.getIssuedQuantity() == null) {
            template.setIssuedQuantity(0);
        }
        couponTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    public void updateTemplate(Long id, CouponTemplateUpdateDTO dto) {
        if (couponTemplateMapper.selectById(id) == null) {
            throw new BusinessException("Template not found");
        }
        CouponTemplate template = new CouponTemplate();
        BeanUtils.copyProperties(dto, template);
        template.setId(id);
        couponTemplateMapper.updateById(template);
    }

    @Override
    public void updateTemplateStatus(Long id, Integer status) {
        if (couponTemplateMapper.selectById(id) == null) {
            throw new BusinessException("Template not found");
        }
        CouponTemplate update = new CouponTemplate();
        update.setId(id);
        update.setStatus(status);
        couponTemplateMapper.updateById(update);
    }

    @Override
    public PageResult<UserCouponVO> pageUserCoupons(UserCouponQueryDTO dto) {
        long pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getTemplateId() != null, UserCoupon::getTemplateId, dto.getTemplateId())
                .eq(dto.getUserId() != null, UserCoupon::getUserId, dto.getUserId())
                .eq(dto.getStatus() != null, UserCoupon::getStatus, dto.getStatus())
                .orderByDesc(UserCoupon::getReceivedTime);
        Page<UserCoupon> page = new Page<>(pageNum, pageSize);
        userCouponMapper.selectPage(page, wrapper);
        List<UserCouponVO> list = page.getRecords().stream().map(this::toUserCouponVO).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    private CouponTemplateVO toTemplateVO(CouponTemplate t) {
        CouponTemplateVO vo = new CouponTemplateVO();
        BeanUtils.copyProperties(t, vo);
        return vo;
    }

    private UserCouponVO toUserCouponVO(UserCoupon c) {
        UserCouponVO vo = new UserCouponVO();
        BeanUtils.copyProperties(c, vo);
        return vo;
    }
}