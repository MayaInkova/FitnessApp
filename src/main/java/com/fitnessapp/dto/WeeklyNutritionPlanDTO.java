package com.fitnessapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fitnessapp.model.DayOfWeek; // Уверете се, че имате такъв enum, ако не - ще го създадем

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeeklyNutritionPlanDTO {
    private Integer id;
    private Integer userId;
    private String userEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<DayOfWeek, NutritionPlanDTO> dailyPlans;
    private Double totalTargetCalories;
    private Double totalProteinGrams;
    private Double totalCarbsGrams;
    private Double totalFatGrams;
}