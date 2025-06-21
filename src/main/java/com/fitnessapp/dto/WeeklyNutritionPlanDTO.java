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
    private Integer id; // Може да има ID, ако го запазваме в база данни
    private Integer userId;
    private String userEmail;
    private LocalDate startDate; // Начална дата на седмичния план (напр. понеделник)
    private LocalDate endDate;   // Крайна дата на седмичния план (напр. неделя)
    private Map<DayOfWeek, NutritionPlanDTO> dailyPlans; // Дневните планове, групирани по ден от седмицата

    // Допълнителни полета за общи сумарни данни за седмицата (опционално)
    private Double totalTargetCalories;
    private Double totalProteinGrams;
    private Double totalCarbsGrams;
    private Double totalFatGrams;
}