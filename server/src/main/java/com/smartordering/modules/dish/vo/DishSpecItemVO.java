package com.smartordering.modules.dish.vo;

import lombok.Data;

import java.util.List;

/**
 * Spec item view object (deserialized from dish.spec_values JSON).
 *
 * @author smartordering
 */
@Data
public class DishSpecItemVO {

    private Long specGroupId;

    private String specGroupName;

    private List<Long> optionIds;

    private List<String> optionNames;
}