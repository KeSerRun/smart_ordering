package com.smartordering.modules.dish.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spec group view object with its options.
 *
 * @author smartordering
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DishSpecGroupVO {

    private Long id;

    private String name;

    private Integer sort;

    private Integer status;

    private LocalDateTime createTime;

    private List<DishSpecOptionVO> options;
}