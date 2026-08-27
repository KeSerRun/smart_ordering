package com.smartordering.modules.coupon.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.coupon.service.CouponService;
import com.smartordering.modules.coupon.vo.CouponTemplateVO;
import com.smartordering.modules.coupon.vo.UserCouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Coupon controller (app side)
 *
 * @author smartordering
 */
@Tag(name = "Coupon (App)")
@RestController
@RequestMapping("/app/coupon")
@RequiredArgsConstructor
public class AppCouponController {

    private final CouponService couponService;

    @Operation(summary = "List available coupon templates")
    @GetMapping("/templates")
    public ApiResponse<List<CouponTemplateVO>> listTemplates() {
        return ApiResponse.ok(couponService.listAvailableTemplates());
    }

    @Operation(summary = "Receive coupon")
    @PostMapping("/{templateId}/receive")
    public ApiResponse<Void> receive(@PathVariable Long templateId) {
        couponService.receive(StpUtil.getLoginIdAsLong(), templateId);
        return ApiResponse.ok();
    }

    @Operation(summary = "List my coupons")
    @GetMapping("/my")
    public ApiResponse<List<UserCouponVO>> myCoupons() {
        return ApiResponse.ok(couponService.listMyCoupons(StpUtil.getLoginIdAsLong()));
    }
}