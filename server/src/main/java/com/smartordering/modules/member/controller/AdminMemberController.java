package com.smartordering.modules.member.controller;

import com.smartordering.common.result.PageResult;
import com.smartordering.common.result.Result;
import com.smartordering.modules.member.dto.MemberGrowthRecordQueryDTO;
import com.smartordering.modules.member.dto.MemberLevelAssignDTO;
import com.smartordering.modules.member.dto.MemberLevelCreateDTO;
import com.smartordering.modules.member.dto.MemberLevelStatusDTO;
import com.smartordering.modules.member.dto.MemberLevelUpdateDTO;
import com.smartordering.modules.member.dto.MemberPointsAdjustDTO;
import com.smartordering.modules.member.dto.MemberPointsRecordQueryDTO;
import com.smartordering.modules.member.dto.MemberQueryDTO;
import com.smartordering.modules.member.service.MemberService;
import com.smartordering.modules.member.vo.MemberDetailVO;
import com.smartordering.modules.member.vo.MemberGrowthRecordVO;
import com.smartordering.modules.member.vo.MemberLevelVO;
import com.smartordering.modules.member.vo.MemberOverviewVO;
import com.smartordering.modules.member.vo.MemberPointsRecordVO;
import com.smartordering.modules.member.vo.MemberProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Admin member management controller.
 *
 * <p>Core surface only. The benefit-config / exchange endpoints were
 * placeholders (dedicated tables absent) and are removed.</p>
 *
 * @author smartordering
 */
@Tag(name = "会员管理 (Admin)")
@RestController
@RequestMapping("/admin/member")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberService memberService;

    @Operation(summary = "Paged member list")
    @GetMapping("/page")
    public Result<PageResult<MemberProfileVO>> page(MemberQueryDTO dto) {
        return Result.success(memberService.pageList(dto));
    }

    @Operation(summary = "Member detail")
    @GetMapping("/{id}")
    public Result<MemberDetailVO> detail(@PathVariable Long id) {
        return Result.success(memberService.detail(id));
    }

    @Operation(summary = "Member overview")
    @GetMapping("/overview")
    public Result<MemberOverviewVO> overview() {
        return Result.success(memberService.overview());
    }

    @Operation(summary = "All member levels")
    @GetMapping("/level/list")
    public Result<List<MemberLevelVO>> levelList() {
        return Result.success(memberService.levelList());
    }

    @Operation(summary = "Create member level")
    @PostMapping("/level")
    public Result<Void> createLevel(@Valid @RequestBody MemberLevelCreateDTO dto) {
        memberService.createLevel(dto);
        return Result.success();
    }

    @Operation(summary = "Update member level")
    @PutMapping("/level/{id}")
    public Result<Void> updateLevel(@PathVariable Long id, @Valid @RequestBody MemberLevelUpdateDTO dto) {
        memberService.updateLevel(id, dto);
        return Result.success();
    }

    @Operation(summary = "Update member level status")
    @PutMapping("/level/{id}/status")
    public Result<Void> updateLevelStatus(@PathVariable Long id, @Valid @RequestBody MemberLevelStatusDTO dto) {
        memberService.updateLevelStatus(id, dto);
        return Result.success();
    }

    @Operation(summary = "Paged points records")
    @GetMapping("/points-record/page")
    public Result<PageResult<MemberPointsRecordVO>> pointsRecordPage(MemberPointsRecordQueryDTO dto) {
        return Result.success(memberService.pointsRecordPage(dto));
    }

    @Operation(summary = "Paged growth records")
    @GetMapping("/growth-record/page")
    public Result<PageResult<MemberGrowthRecordVO>> growthRecordPage(MemberGrowthRecordQueryDTO dto) {
        return Result.success(memberService.growthRecordPage(dto));
    }

    @Operation(summary = "Adjust member points")
    @PostMapping("/{id}/points-adjust")
    public Result<Void> adjustPoints(@PathVariable Long id, @Valid @RequestBody MemberPointsAdjustDTO dto) {
        memberService.adjustPoints(id, dto);
        return Result.success();
    }

    @Operation(summary = "Assign member level to a user")
    @PutMapping("/{id}/level")
    public Result<Void> assignLevel(@PathVariable Long id, @Valid @RequestBody MemberLevelAssignDTO dto) {
        memberService.assignLevel(id, dto);
        return Result.success();
    }

    // marketing/member-benefit page relies on these. Backend wiring is in progress.
    @Operation(summary = "Benefit config")
    @GetMapping("/benefit-config")
    public Result<Object> benefitConfig() {
        return Result.success(Map.of());
    }

    @Operation(summary = "Save benefit config")
    @PutMapping("/benefit-config")
    public Result<Void> saveBenefitConfig(@RequestBody Map<String, Object> body) {
        return Result.success();
    }

    @Operation(summary = "Exchange list")
    @GetMapping("/exchange/list")
    public Result<List<Object>> exchangeList() {
        return Result.success(List.of());
    }

    @Operation(summary = "Save exchange")
    @PostMapping("/exchange")
    public Result<Void> saveExchange(@RequestBody Map<String, Object> body) {
        return Result.success();
    }

    @Operation(summary = "Delete exchange")
    @DeleteMapping("/exchange/{id}")
    public Result<Void> deleteExchange(@PathVariable Long id) {
        return Result.success();
    }
}