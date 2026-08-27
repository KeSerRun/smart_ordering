package com.smartordering.modules.dish.controller;

import com.smartordering.common.result.ApiResponse;
import com.smartordering.modules.dish.dto.DishSpecGroupDTO;
import com.smartordering.modules.dish.service.DishSpecGroupService;
import com.smartordering.modules.dish.vo.DishSpecGroupVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin dish spec group management controller.
 *
 * @author smartordering
 */
@Tag(name = "Dish Spec Group (Admin)")
@RestController
@RequestMapping("/admin/dish/spec")
@RequiredArgsConstructor
public class AdminDishSpecController {

    private final DishSpecGroupService dishSpecGroupService;

    @Operation(summary = "List all spec groups with options (admin)")
    @GetMapping("/list")
    public ApiResponse<List<DishSpecGroupVO>> listAll() {
        return ApiResponse.ok(dishSpecGroupService.listGroups());
    }

    @Operation(summary = "Create spec group")
    @PostMapping
    public ApiResponse<Long> create(@RequestBody DishSpecGroupDTO dto) {
        return ApiResponse.ok(dishSpecGroupService.createGroup(dto));
    }

    @Operation(summary = "Update spec group")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody DishSpecGroupDTO dto) {
        dto.setId(id);
        dishSpecGroupService.updateGroup(dto);
        return ApiResponse.ok();
    }

    @Operation(summary = "Delete spec group")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dishSpecGroupService.deleteGroup(id);
        return ApiResponse.ok();
    }
}