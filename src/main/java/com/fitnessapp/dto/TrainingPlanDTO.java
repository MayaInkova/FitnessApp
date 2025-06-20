package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitnessapp.model.TrainingType; // ИМПОРТИРАМЕ!
import com.fitnessapp.model.LevelType; // ИМПОРТИРАМЕ!

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainingPlanDTO {
    private Integer id;
    private LocalDate dateGenerated;
    private String trainingPlanDescription; // Добавено поле за описание
    private Integer daysPerWeek;
    private Integer durationMinutes;
    private Integer userId;
    private String userEmail;
    private List<TrainingSessionDTO> trainingSessions;

    private String userGenderSnapshot;
    private Integer userAgeSnapshot;
    private Double userWeightSnapshot;
    private Double userHeightSnapshot;
    private String userActivityLevelSnapshotName;
    private TrainingType userTrainingTypeSnapshot;
    private LevelType userLevelSnapshot; // НОВО: snapshot на нивото на потребителя
    private String userGoalNameSnapshot; // НОВО: snapshot на целта на потребителя (име)
}