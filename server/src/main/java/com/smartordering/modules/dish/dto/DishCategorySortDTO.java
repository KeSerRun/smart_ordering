package com.smartordering.modules.dish.dto;

import lombok.Data;

import java.util.List;

/**
 * Batch update of category sort order.
 *
 * @author smartordering
 */
@Data
public class DishCategorySortDTO {

    private List<SortItem> items;

    @Data
    public static class SortItem {
        private Long id;
        private Integer sort;
    }
}