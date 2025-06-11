package com.fitnessapp.dto;

import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.TrainingPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanBundleResponse {
    private NutritionPlan nutritionPlan;
    private TrainingPlan trainingPlan;
}