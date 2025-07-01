package com.fitnessapp.dto;

import com.fitnessapp.model.ActivityLevel;
import com.fitnessapp.model.DietType;
import com.fitnessapp.model.GenderType;
import com.fitnessapp.model.Goal;
import com.fitnessapp.model.LevelType;
import com.fitnessapp.model.MealFrequencyPreferenceType;
import com.fitnessapp.model.MeatPreferenceType;
import com.fitnessapp.model.TrainingType;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserUpdateRequest {

    private String fullName;
    private String email;

    private Integer age;
    private Double height;
    private Double weight;
    private GenderType gender;

    private Integer activityLevelId;
    private Integer goalId;
    private Integer dietTypeId;

    private TrainingType trainingType;
    private Integer trainingDaysPerWeek;
    private Integer trainingDurationMinutes;
    private LevelType level;

    private Set<String> allergies;
    private MeatPreferenceType meatPreference;
    private Boolean consumesDairy;
    private MealFrequencyPreferenceType mealFrequencyPreference;
    private Set<String> otherDietaryPreferences;
}