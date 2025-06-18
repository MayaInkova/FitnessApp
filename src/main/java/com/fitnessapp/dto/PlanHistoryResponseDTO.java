package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanHistoryResponseDTO {
    private List<NutritionPlanHistoryDTO> nutritionPlans;
    private List<TrainingPlanHistoryDTO> trainingPlans;
}