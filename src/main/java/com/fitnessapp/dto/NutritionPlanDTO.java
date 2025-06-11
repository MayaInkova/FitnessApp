package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // Добавено NoArgsConstructor за Lombok

@Data
@AllArgsConstructor
@NoArgsConstructor // Добра практика за DTOs, особено ако Lombok го ползва
public class NutritionPlanDTO {
    private Integer id; // ID е Integer
    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbs;
    private String goalName; // Име на целта (String)
}
