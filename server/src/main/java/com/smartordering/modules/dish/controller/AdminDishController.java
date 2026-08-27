package com.smartordering.modules.dish.controller;

import com.smartordering.common.result.ApiResponse;
import com.smartordering.common.result.PageVO;
import com.smartordering.modules.dish.dto.DishAdminQueryDTO;
import com.smartordering.modules.dish.dto.DishCreateDTO;
import com.smartordering.modules.dish.service.DishService;
import com.smartordering.modules.dish.vo.AdminDishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Admin dish management controller.
 *
 * @author smartordering
 */
@Tag(name = "Dish (Admin)")
@RestController
@RequestMapping("/admin/dish")
@RequiredArgsConstructor
public class AdminDishController {

    private final DishService dishService;

    @Operation(summary = "Paged dish list (admin)")
    @GetMapping("/list")
    public ApiResponse<PageVO<AdminDishVO>> list(DishAdminQueryDTO query) {
        return ApiResponse.ok(dishService.pageQuery(query));
    }

    @Operation(summary = "Dish detail (admin)")
    @GetMapping("/{id}")
    public ApiResponse<AdminDishVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(dishService.getAdminDetail(id));
    }

    @Operation(summary = "Create dish")
    @PostMapping
    public ApiResponse<Long> create(@RequestBody DishCreateDTO dto) {
        return ApiResponse.ok(dishService.createDish(dto));
    }

    @Operation(summary = "Update dish")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody DishCreateDTO dto) {
        dto.setId(id);
        dishService.updateDish(id, dto);
        return ApiResponse.ok();
    }

    @Operation(summary = "Update dish on/off shelf status")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id,
                                          @RequestParam Integer status) {
        dishService.updateDishStatus(id, status);
        return ApiResponse.ok();
    }
}