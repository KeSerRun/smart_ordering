package com.smartordering.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login request DTO
 *
 * @author smartordering
 */
@Data
public class LoginDTO {

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}