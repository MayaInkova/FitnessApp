package com.fitnessapp.dto;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

    @NotBlank(message = "Името е задължително.")
    private String fullName;

    @Email(message = "Невалиден имейл.")
    @NotBlank(message = "Имейлът е задължителен.")
    private String email;

    @Size(min = 6, message = "Паролата трябва да е поне 6 символа.")
    private String password;

    @NotNull(message = "Възрастта е задължителна.")
    private Integer age;

    @NotNull(message = "Височината е задължителна.")
    private Double height;

    @NotNull(message = "Теглото е задължително.")
    private Double weight;

    @NotBlank(message = "Полът е задължителен.")
    private String gender;

    @NotBlank(message = "Нивото на активност е задължително.")
    private String activityLevel;

    @NotBlank(message = "Целта е задължителна.")
    private String goal;
}