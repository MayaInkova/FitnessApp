package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fitnessapp.model.DayOfWeek;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSessionDTO {
    private Integer id;
    private DayOfWeek dayOfWeek;
    private Integer durationMinutes;
    private List<ExerciseDTO> exercises;
}