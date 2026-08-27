package com.smartordering.modules.dish.controller;

import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.dish.dto.DishCategoryAdminDTO;
import com.smartordering.modules.dish.dto.DishCategorySortDTO;
import com.smartordering.modules.dish.service.DishCategoryService;
import com.smartordering.modules.dish.vo.AdminDishCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin dish category management controller.
 *
 * @author smartordering
 */
@Tag(name = "Dish Category (Admin)")
@RestController
@RequestMapping("/admin/dish/category")
@RequiredArgsConstructor
public class AdminDishCategoryController {

    private final DishCategoryService dishCategoryService;

    @Operation(summary = "List all categories with spec bindings (admin)")
    @GetMapping("/list")
    public ApiResponse<List<AdminDishCategoryVO>> listAll() {
        return ApiResponse.ok(dishCategoryService.listAdmin());
    }

    @Operation(summary = "Create category")
    @PostMapping
    public ApiResponse<Long> create(@RequestBody DishCategoryAdminDTO dto) {
        return ApiResponse.ok(dishCategoryService.createCategory(dto));
    }

    @Operation(summary = "Update category")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody DishCategoryAdminDTO dto) {
        dto.setId(id);
        dishCategoryService.updateCategory(dto);
        return ApiResponse.ok();
    }

    @Operation(summary = "Delete category")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dishCategoryService.deleteCategory(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "Batch update category sort")
    @PutMapping("/sort")
    public ApiResponse<Void> updateSort(@RequestBody DishCategorySortDTO dto) {
        dishCategoryService.updateSort(dto);
        return ApiResponse.ok();
    }
}