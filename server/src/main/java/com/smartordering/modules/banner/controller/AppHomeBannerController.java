package com.smartordering.modules.banner.controller;

import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.banner.service.HomeBannerService;
import com.smartordering.modules.banner.vo.HomeBannerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Home banner controller (app side)
 *
 * @author smartordering
 */
@Tag(name = "Banner (App)")
@RestController
@RequestMapping("/app/banner")
@RequiredArgsConstructor
public class AppHomeBannerController {

    private final HomeBannerService homeBannerService;

    @Operation(summary = "List enabled banners")
    @GetMapping("/list")
    public ApiResponse<List<HomeBannerVO>> listEnabled(@RequestParam(required = false) String scene) {
        return ApiResponse.ok(homeBannerService.listEnabled(scene));
    }
}