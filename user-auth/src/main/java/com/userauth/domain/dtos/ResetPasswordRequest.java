package com.userauth.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    private String newPassword;
}
