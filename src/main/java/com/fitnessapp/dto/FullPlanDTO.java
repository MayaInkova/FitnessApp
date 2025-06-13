package com.fitnessapp.dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FullPlanDTO {
    private Integer nutritionPlanId;
    private Double targetCalories;
    private Double protein;
    private Double fat;
    private Double carbohydrates;
    private String goalName;
    private List<MealDTO> meals;

    private Integer trainingPlanId;
    private String trainingPlanDescription;
    private Integer trainingDaysPerWeek;
    private Integer trainingDurationMinutes;
    private List<TrainingSessionDTO> trainingSessions;
}
