package com.smartordering.modules.review.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Review create DTO
 *
 * @author smartordering
 */
@Data
public class ReviewCreateDTO {

    @NotNull(message = "Order id cannot be null")
    private Long orderId;

    @NotNull(message = "Overall rating cannot be null")
    @Min(value = 1, message = "Rating min is 1")
    @Max(value = 5, message = "Rating max is 5")
    private Integer overallRating;

    private String content;

    @Valid
    private List<ItemRatingDTO> itemRatings;

    /**
     * Item rating DTO
     */
    @Data
    public static class ItemRatingDTO {

        @NotNull(message = "Order item id cannot be null")
        private Long orderItemId;

        @NotNull(message = "Rating cannot be null")
        @Min(value = 1, message = "Rating min is 1")
        @Max(value = 5, message = "Rating max is 5")
        private Integer rating;
    }
}