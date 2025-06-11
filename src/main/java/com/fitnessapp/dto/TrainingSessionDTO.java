package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSessionDTO {
    private Integer id;
    // Няма нужда от trainingPlanId тук, тъй като е вложен в TrainingPlanDTO
    private DayOfWeek dayOfWeek;
    private Integer durationMinutes;
    private List<ExerciseDTO> exercises; // Влагаме ExerciseDTO
}