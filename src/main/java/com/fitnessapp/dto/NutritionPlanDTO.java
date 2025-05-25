package com.fitnessapp.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NutritionPlanDTO {
    private Integer id;
    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbs;
    private String goal;
}