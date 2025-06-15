package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class NutritionPlanRequest {
    private Integer userId;
    private Integer age;
    private Double height;
    private Double weight;
    private String goalName;
    private String gender;
    private String dietTypeName;

}