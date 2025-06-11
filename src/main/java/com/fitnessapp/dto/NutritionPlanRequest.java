package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder; // Добавен Builder
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Добавен Builder за удобство

public class NutritionPlanRequest {
    private Integer userId; // Променено на Integer
    private Integer age; // Променено на Integer за консистентност
    private Double height;
    private Double weight;
    private String goalName; // Използваме името на целта от Goal Entity
    private String gender; // GenderType enum ще бъде преобразуван от/към String
    private String dietTypeName; // DietType Entity име
    // Може да се добави и activityLevel, ако е необходимо за плана
}