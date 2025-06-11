package com.fitnessapp.dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FullPlanDTO {
    // Nutrition Plan Details
    private Integer nutritionPlanId; // ID на хранителния план
    private Double targetCalories;
    private Double protein;
    private Double fat;
    private Double carbohydrates;
    private String goalName; // Име на целта, а не целия обект
    private List<MealDTO> meals; // Влагаме MealDTO, а не Meal Entity

    // Training Plan Details
    private Integer trainingPlanId; // ID на тренировъчния план
    private String trainingPlanDescription; // Описание на тренировъчния план, ако има
    private Integer trainingDaysPerWeek;
    private Integer trainingDurationMinutes;
    private List<TrainingSessionDTO> trainingSessions; // Влагаме TrainingSessionDTO, а не TrainingSession Entity
}