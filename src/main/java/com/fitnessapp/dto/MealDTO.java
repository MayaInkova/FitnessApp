package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fitnessapp.model.MealType; // Използваме MealType enum

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealDTO {
    private Integer id;
    // Няма нужда от nutritionPlanId тук, тъй като MealDTO е вложен в NutritionPlanDTO
    private RecipeDTO recipe; // Влагаме RecipeDTO
    private MealType mealType; // Използваме enum
    private Double portionSize;
}