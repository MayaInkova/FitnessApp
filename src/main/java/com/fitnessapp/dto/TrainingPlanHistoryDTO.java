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
public class TrainingPlanHistoryDTO {
    private Integer id;
    private LocalDate dateGenerated;
    private String trainingPlanDescription;
    private Integer trainingDaysPerWeek;
    private Integer trainingDurationMinutes;
    private String userGenderSnapshot;
    private Integer userAgeSnapshot;
    private Double userWeightSnapshot;
    private Double userHeightSnapshot;
    private String userActivityLevelSnapshotName;
}