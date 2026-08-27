package com.smartordering.modules.dish.controller;

import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.dish.service.DishCategoryService;
import com.smartordering.modules.dish.vo.DishCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Dish category controller (app side)
 *
 * @author smartordering
 */
@Tag(name = "Dish Category (App)")
@RestController
@RequestMapping("/app/dish/category")
@RequiredArgsConstructor
public class AppDishCategoryController {

    private final DishCategoryService dishCategoryService;

    @Operation(summary = "List enabled categories")
    @GetMapping("/list")
    public ApiResponse<List<DishCategoryVO>> list() {
        return ApiResponse.ok(dishCategoryService.listEnabled());
    }
}