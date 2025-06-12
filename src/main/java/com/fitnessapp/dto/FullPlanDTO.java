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
    private String goalName;
    private List<MealDTO> meals;

    // Training Plan Details
    private Integer trainingPlanId; // ID на тренировъчния план
    private String trainingPlanDescription;
    private Integer trainingDaysPerWeek;
    private Integer trainingDurationMinutes;
    private List<TrainingSessionDTO> trainingSessions;
}