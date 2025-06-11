package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class TrainingPlanDTO {
    private Integer id;

    private LocalDate dateGenerated;
    private Integer daysPerWeek;
    private Integer durationMinutes;
    private List<TrainingSessionDTO> trainingSessions;
}