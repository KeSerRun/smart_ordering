package com.smartordering.modules.member.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.member.service.MemberService;
import com.smartordering.modules.member.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Member service interface
 *
 * @author smartordering
 */
@RestController
@RequestMapping("/app/member")
@RequiredArgsConstructor
public class AppMemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ApiResponse<MemberCenterVO> me() {
        return ApiResponse.ok(memberService.getMemberCenter(StpUtil.getLoginIdAsLong()));
    }

    @GetMapping("/level/list")
    public ApiResponse<List<MemberLevelVO>> levelList() {
        return ApiResponse.ok(memberService.listEnabledLevels());
    }

    @GetMapping("/points-record/page")
    public ApiResponse<IPage<MemberPointsRecordVO>> pointsPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(memberService.pagePointsRecords(StpUtil.getLoginIdAsLong(), pageNum, pageSize));
    }

    @GetMapping("/growth-record/page")
    public ApiResponse<IPage<MemberGrowthRecordVO>> growthPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(memberService.pageGrowthRecords(StpUtil.getLoginIdAsLong(), pageNum, pageSize));
    }
}