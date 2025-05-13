package com.fitnessapp.dto;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserResponse {
    private Integer id;
    private String fullName;
    private String email;
    private Integer age;
    private Double height;
    private Double weight;
    private String gender;
    private String activityLevel;
    private String goal;
}