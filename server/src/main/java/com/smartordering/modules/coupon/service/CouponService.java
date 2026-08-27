package com.smartordering.modules.coupon.service;

import com.smartordering.common.result.PageResult;
import com.smartordering.modules.coupon.dto.CouponTemplateCreateDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateQueryDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateUpdateDTO;
import com.smartordering.modules.coupon.dto.UserCouponQueryDTO;
import com.smartordering.modules.coupon.vo.CouponTemplateVO;
import com.smartordering.modules.coupon.vo.UserCouponVO;

import java.util.List;

/**
 * Coupon service interface.
 *
 * @author smartordering
 */
public interface CouponService {

    List<CouponTemplateVO> listAvailableTemplates();

    void receive(Long userId, Long templateId);

    List<UserCouponVO> listMyCoupons(Long userId);

    // ===== admin =====

    PageResult<CouponTemplateVO> pageTemplates(CouponTemplateQueryDTO dto);

    Long createTemplate(CouponTemplateCreateDTO dto);

    void updateTemplate(Long id, CouponTemplateUpdateDTO dto);

    void updateTemplateStatus(Long id, Integer status);

    PageResult<UserCouponVO> pageUserCoupons(UserCouponQueryDTO dto);
}