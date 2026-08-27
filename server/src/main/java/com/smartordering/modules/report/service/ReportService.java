package com.smartordering.modules.report.service;

import com.smartordering.modules.report.vo.DishRankingVO;
import com.smartordering.modules.report.vo.RevenueVO;
import com.smartordering.modules.report.vo.TableTurnoverVO;

import java.time.LocalDate;
import java.util.List;

/**
 * Report service interface
 *
 * @author smartordering
 */
public interface ReportService {

    List<RevenueVO> getRevenue(String dimension, LocalDate startDate, LocalDate endDate);

    List<DishRankingVO> getDishRanking(LocalDate startDate, LocalDate endDate, Integer limit);

    List<TableTurnoverVO> getTableTurnover(LocalDate startDate, LocalDate endDate);
}