package com.smartordering.modules.dish.dto;

import lombok.Data;

import java.util.List;

/**
 * One spec item carried on a dish (create/update payload).
 *
 * @author smartordering
 */
@Data
public class DishSpecItemDTO {

    private Long specGroupId;

    private String specGroupName;

    private List<Long> optionIds;

    private List<String> optionNames;
}