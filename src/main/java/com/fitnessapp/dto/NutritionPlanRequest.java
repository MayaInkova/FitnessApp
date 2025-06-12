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
    private String goalName; // Използваме името на целта от Goal Entity
    private String gender; // GenderType enum ще бъде преобразуван от/към String
    private String dietTypeName; // DietType Entity име

}