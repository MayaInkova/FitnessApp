package com.fitnessapp.dto;

import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.TrainingPlan;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlanBundleResponse {
    private NutritionPlan nutritionPlan;
    private TrainingPlan trainingPlan;
}