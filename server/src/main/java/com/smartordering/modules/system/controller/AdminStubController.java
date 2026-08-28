package com.smartordering.modules.system.controller;

import com.smartordering.common.result.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Admin stub controller: returns placeholder data for admin-side APIs
 * that have not been implemented yet. Replace with real implementations later.
 *
 * @author smartordering
 */
@RestController
@RequestMapping("/admin")
public class AdminStubController {

    /** Home dashboard overview (report module) */
    @GetMapping("/report/dashboard-overview")
    public ApiResponse<Map<String, Object>> dashboardOverview() {
        return ApiResponse.ok(Map.of(
            "todayRevenue", 0,
            "yesterdayRevenue", 0,
            "todayOrderCount", 0,
            "averageTicket", 0,
            "occupancyRate", 0,
            "tableStats", Map.of("total", 25, "free", 25, "occupied", 0),
            "revenueTrend", List.of(),
            "dishRanking", List.of(),
            "alerts", List.of()
        ));
    }
}