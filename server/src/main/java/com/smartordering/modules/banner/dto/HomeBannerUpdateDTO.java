package com.smartordering.modules.banner.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 首页轮播图更新 DTO
 *

 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HomeBannerUpdateDTO extends HomeBannerCreateDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 轮播图ID
     */
    @NotNull(message = "轮播图ID不能为空")
    private Long id;
}
