package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder // Добавен Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NutritionPlanDTO {
    private Integer id;
    private LocalDate dateGenerated;
    private Double targetCalories;
    private Double protein;
    private Double fat;
    private Double carbohydrates;
    private String goalName;
    private Integer userId;
    private String userEmail;
    private List<MealDTO> meals;
    private String dietTypeName;
    private String dietTypeDescription;
}