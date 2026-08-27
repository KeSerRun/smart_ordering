package com.smartordering.modules.dish.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartordering.modules.system.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Binding entity between a dish category and a spec group (many-to-many).
 *
 * @author smartordering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dish_category_spec")
public class DishCategorySpec extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Category ID */
    private Long categoryId;

    /** Spec group ID */
    private Long specGroupId;
}