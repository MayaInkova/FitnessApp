package com.fitnessapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitnessapp.model.GenderType;
import com.fitnessapp.model.LevelType;
import com.fitnessapp.model.MeatPreferenceType;
import com.fitnessapp.model.MealFrequencyPreferenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO {
    private Integer id;
    private String fullName;
    private String email;
    private Integer age;
    private Double height;
    private Double weight;
    private GenderType gender;


    private Integer activityLevelId;
    private String activityLevelName;

    private Integer goalId;
    private String goalName;

    private Integer dietTypeId;
    private String dietTypeName;

    private String trainingType;
    private Integer trainingDaysPerWeek;
    private Integer trainingDurationMinutes;
    private LevelType level;


    private Set<String> allergies;
    private MeatPreferenceType meatPreference;
    private Boolean consumesDairy;
    private MealFrequencyPreferenceType mealFrequencyPreference;
    private Set<String> otherDietaryPreferences;


    private Set<String> roles;

    private Integer nutritionPlanId;
    private Integer trainingPlanId;
}