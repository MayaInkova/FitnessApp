package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

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
    private Integer daysPerWeek;
    private Integer durationMinutes;
    private Integer userId;
    private String userEmail;
    private List<TrainingSessionDTO> trainingSessions;
}