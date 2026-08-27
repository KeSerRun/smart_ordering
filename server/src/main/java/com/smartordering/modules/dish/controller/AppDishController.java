package com.smartordering.modules.dish.controller;

import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.dish.service.DishService;
import com.smartordering.modules.dish.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dish controller (app side)
 *
 * @author smartordering
 */
@Tag(name = "Dish (App)")
@RestController
@RequestMapping("/app/dish")
@RequiredArgsConstructor
public class AppDishController {

    private final DishService dishService;

    @Operation(summary = "List on-sale dishes (optional categoryId)")
    @GetMapping("/list")
    public ApiResponse<List<DishVO>> list(@RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(dishService.listOnSale(categoryId));
    }

    @Operation(summary = "Get dish detail")
    @GetMapping("/{id}")
    public ApiResponse<DishVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(dishService.getDetail(id));
    }

    @Operation(summary = "Set/clear dish sold-out flag (0=normal 1=sold out)")
    @PutMapping("/{id}/sold-out")
    public ApiResponse<Void> updateSoldOut(@PathVariable Long id,
                                           @RequestParam Integer soldOut) {
        dishService.updateDishSoldOut(id, soldOut);
        return ApiResponse.ok();
    }
}