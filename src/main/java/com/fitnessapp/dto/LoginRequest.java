package com.fitnessapp.dto;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @Email(message = "Невалиден имейл.")
    @NotBlank(message = "Имейлът е задължителен.")
    private String email;

    @NotBlank(message = "Паролата е задължителна.")
    private String password;
}