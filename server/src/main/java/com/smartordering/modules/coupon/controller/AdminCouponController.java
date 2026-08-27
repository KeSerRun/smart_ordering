package com.smartordering.modules.coupon.controller;

import com.smartordering.common.result.PageResult;
import com.smartordering.common.result.Result;
import com.smartordering.modules.coupon.dto.CouponGrantDTO;
import com.smartordering.modules.coupon.dto.CouponGrantTaskQueryDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateCreateDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateQueryDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateUpdateDTO;
import com.smartordering.modules.coupon.dto.UserCouponQueryDTO;
import com.smartordering.modules.coupon.service.CouponService;
import com.smartordering.modules.coupon.vo.CouponGrantTaskVO;
import com.smartordering.modules.coupon.vo.CouponTemplateVO;
import com.smartordering.modules.coupon.vo.UserCouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // The manage/coupon page calls these. Backend wiring is in progress.
    @Operation(summary = "Grant coupons")
    @PostMapping("/grant")
    public Result<CouponGrantTaskVO> grantCoupons(@RequestBody CouponGrantDTO dto) {
        CouponGrantTaskVO vo = new CouponGrantTaskVO();
        vo.setTaskStatus(2);
        vo.setSuccessCount(0);
        return Result.success(vo);
    }

    @Operation(summary = "Paged grant tasks")
    @GetMapping("/task/page")
    public Result<PageResult<CouponGrantTaskVO>> pageGrantTasks(CouponGrantTaskQueryDTO dto) {
        return Result.success(PageResult.of(List.of(), 1L, 10L, 0L));
    }

    @Operation(summary = "Paged grant task details")
    @GetMapping("/task/detail/page")
    public Result<PageResult<String>> pageGrantTaskDetails() {
        return Result.success(PageResult.of(List.of(), 1L, 10L, 0L));
    }
}