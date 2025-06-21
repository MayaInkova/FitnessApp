package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fitnessapp.model.ExerciseType;
import com.fitnessapp.model.DifficultyLevel;
import com.fitnessapp.model.EquipmentType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer sets;
    private Integer reps;
    private Integer durationMinutes;
    private ExerciseType type; // ПРОМЕНЕНО: От String на ExerciseType
    private DifficultyLevel difficultyLevel; // ПРОМЕНЕНО: От String на DifficultyLevel
    private EquipmentType equipment; // ПРОМЕНЕНО: От String на EquipmentType
}