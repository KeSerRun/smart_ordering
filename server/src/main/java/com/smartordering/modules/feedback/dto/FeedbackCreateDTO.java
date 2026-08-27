package com.smartordering.modules.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Feedback create DTO
 *
 * @author smartordering
 */
@Data
public class FeedbackCreateDTO {

    @NotBlank(message = "Content cannot be empty")
    private String content;

    private String contactPhone;
}