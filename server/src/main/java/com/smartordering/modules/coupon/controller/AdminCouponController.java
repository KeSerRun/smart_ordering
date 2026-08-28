package com.smartordering.modules.coupon.controller;

import com.smartordering.common.result.PageResult;
import com.smartordering.common.result.Result;
import com.smartordering.modules.coupon.dto.CouponGrantDTO;
import com.smartordering.modules.coupon.dto.CouponGrantTaskDetailQueryDTO;
import com.smartordering.modules.coupon.dto.CouponGrantTaskQueryDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateCreateDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateQueryDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateUpdateDTO;
import com.smartordering.modules.coupon.dto.UserCouponQueryDTO;
import com.smartordering.modules.coupon.service.CouponService;
import com.smartordering.modules.coupon.vo.CouponGrantTaskDetailVO;
import com.smartordering.modules.coupon.vo.CouponGrantTaskVO;
import com.smartordering.modules.coupon.vo.CouponTemplateVO;
import com.smartordering.modules.coupon.vo.UserCouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Admin coupon controller.
 *
 * @author smartordering
 */
@Tag(name = "优惠券 (Admin)")
@RestController
@RequestMapping("/admin/coupon")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @Operation(summary = "Paged coupon templates")
    @GetMapping("/template/page")
    public Result<PageResult<CouponTemplateVO>> pageTemplates(CouponTemplateQueryDTO dto) {
        return Result.success(couponService.pageTemplates(dto));
    }

    @Operation(summary = "Create coupon template")
    @PostMapping("/template")
    public Result<Void> createTemplate(@Valid @RequestBody CouponTemplateCreateDTO dto) {
        couponService.createTemplate(dto);
        return Result.success();
    }

    @Operation(summary = "Update coupon template")
    @PutMapping("/template/{id}")
    public Result<Void> updateTemplate(@PathVariable Long id, @Valid @RequestBody CouponTemplateUpdateDTO dto) {
        couponService.updateTemplate(id, dto);
        return Result.success();
    }

    @Operation(summary = "Update coupon template status")
    @PutMapping("/template/{id}/status")
    public Result<Void> updateTemplateStatus(@PathVariable Long id, @RequestParam Integer status) {
        couponService.updateTemplateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "Paged user coupons")
    @GetMapping("/user/page")
    public Result<PageResult<UserCouponVO>> pageUserCoupons(UserCouponQueryDTO dto) {
        return Result.success(couponService.pageUserCoupons(dto));
    }

    @Operation(summary = "Revoke a user coupon (unused only)")
    @DeleteMapping("/user/{userCouponId}/revoke")
    public Result<Void> revokeUserCoupon(@PathVariable Long userCouponId) {
        couponService.revokeUserCoupon(userCouponId);
        return Result.success();
    }

    // ===== 发券任务 =====

    @Operation(summary = "Submit coupon grant task (async, supports by-level grants)")
    @PostMapping("/grant")
    public Result<CouponGrantTaskVO> grantCoupons(@Valid @RequestBody CouponGrantDTO dto) {
        return Result.success(couponService.submitGrantTask(dto));
    }

    @Operation(summary = "Get coupon grant task status")
    @GetMapping("/task/{id}")
    public Result<CouponGrantTaskVO> getGrantTask(@PathVariable Long id) {
        return Result.success(couponService.getGrantTask(id));
    }

    @Operation(summary = "Paged coupon grant tasks")
    @GetMapping("/task/page")
    public Result<PageResult<CouponGrantTaskVO>> pageGrantTasks(CouponGrantTaskQueryDTO dto) {
        return Result.success(couponService.pageGrantTasks(dto));
    }

    @Operation(summary = "Paged coupon grant task details")
    @GetMapping("/task/detail/page")
    public Result<PageResult<CouponGrantTaskDetailVO>> pageGrantTaskDetails(CouponGrantTaskDetailQueryDTO dto) {
        return Result.success(couponService.pageGrantTaskDetails(dto));
    }
}