package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fitnessapp.model.MealType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealDTO {
    private Integer id;
    private RecipeDTO recipe;
    private MealType mealType;
    private Double portionSize;

    // НОВО: Изчислени полета за тази конкретна порция
    private Double calculatedCalories;
    private Double calculatedProtein;
    private Double calculatedCarbs;
    private Double calculatedFat;
    private Boolean hasAlternatives;
}