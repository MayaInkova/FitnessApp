package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NutritionPlanHistoryDTO {
    private Integer id;
    private LocalDate dateGenerated;
    private Double targetCalories;
    private Double protein;
    private Double fat;
    private Double carbohydrates;
    private String goalName;
    private String userGenderSnapshot;
    private Integer userAgeSnapshot;
    private Double userWeightSnapshot;
    private Double userHeightSnapshot;
    private String userActivityLevelSnapshotName;
    private String userDietTypeSnapshotName;
    private String userAllergiesSnapshot;
    private String userOtherDietaryPreferencesSnapshot;
    private String userMeatPreferenceSnapshot;
    private Boolean userConsumesDairySnapshot;
    private String userMealFrequencyPreferenceSnapshot;

}