package com.smartordering.modules.report.controller;

import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.report.service.ReportService;
import com.smartordering.modules.report.vo.DishRankingVO;
import com.smartordering.modules.report.vo.RevenueVO;
import com.smartordering.modules.report.vo.TableTurnoverVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Report controller (admin side)
 *
 * @author smartordering
 */
@Tag(name = "Report (Admin)")
@RestController
@RequestMapping("/admin/report")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    @Operation(summary = "Revenue statistics")
    @GetMapping("/revenue")
    public ApiResponse<List<RevenueVO>> getRevenue(
            @RequestParam(defaultValue = "day") String dimension,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResponse.ok(reportService.getRevenue(dimension, startDate, endDate));
    }

    @Operation(summary = "Dish sales ranking")
    @GetMapping("/dish-ranking")
    public ApiResponse<List<DishRankingVO>> getDishRanking(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "20") Integer limit) {
        return ApiResponse.ok(reportService.getDishRanking(startDate, endDate, limit));
    }

    @Operation(summary = "Table turnover rate")
    @GetMapping("/table-turnover")
    public ApiResponse<List<TableTurnoverVO>> getTableTurnover(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResponse.ok(reportService.getTableTurnover(startDate, endDate));
    }
}