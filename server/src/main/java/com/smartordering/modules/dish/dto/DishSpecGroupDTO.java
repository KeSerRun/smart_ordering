package com.smartordering.modules.dish.dto;

import lombok.Data;

import java.util.List;

/**
 * Spec group create/update payload. {@code id} is present when updating.
 *
 * @author smartordering
 */
@Data
public class DishSpecGroupDTO {

    /** Present when updating */
    private Long id;

    private String name;

    private Integer sort;

    private Integer status;

    private List<OptionDTO> options;

    @Data
    public static class OptionDTO {
        /** Present when the option already exists and keeps its id */
        private Long id;
        private String name;
        private Integer sort;
    }
}