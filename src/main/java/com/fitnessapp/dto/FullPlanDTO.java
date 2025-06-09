package com.fitnessapp.dto;

import com.fitnessapp.model.Meal;
import com.fitnessapp.model.Recipe;
import com.fitnessapp.model.Exercise;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FullPlanDTO {
    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbs;
    private String goal;
    private List<Meal> meals;
    private List<Recipe> recipes;

    private String trainingName;
    private String trainingDescription;
    private int daysPerWeek;
    private int durationMinutes;
    private List<Exercise> exercises;
}