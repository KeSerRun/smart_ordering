package com.smartordering.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Register request DTO
 *
 * @author smartordering
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    private String nickname;
}