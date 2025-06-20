package com.fitnessapp.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String token; // Токенът от URL-а

    @NotBlank
    @Size(min = 6, max = 40) // Валидация за минимална/максимална дължина
    private String newPassword;
}