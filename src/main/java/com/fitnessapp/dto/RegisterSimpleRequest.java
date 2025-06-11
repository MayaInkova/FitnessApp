package com.fitnessapp.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterSimpleRequest {

    @NotBlank(message = "Името е задължително.")
    private String fullName;

    @Email(message = "Невалиден имейл адрес.")
    @NotBlank(message = "Имейлът е задължителен.")
    private String email;

    @Size(min = 6, message = "Паролата трябва да е поне 6 символа.")
    private String password;
}