package com.fitnessapp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Set;
import com.fitnessapp.model.LevelType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdateDto {
    private String fullName;
    private String gender;
    private Integer age;
    private Double height;
    private Double weight;
    private Set<String> allergies;
    private String goalName;
    private String activityLevelName;
    private String dietTypeName;
    private Boolean consumesDairy;
    private String meatPreference;
    private String trainingType;
    private String mealFrequencyPreference;
    private Integer trainingDaysPerWeek;
    private Integer trainingDurationMinutes;
    private LevelType level;
}