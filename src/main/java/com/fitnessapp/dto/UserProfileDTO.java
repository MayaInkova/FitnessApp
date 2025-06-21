package com.fitnessapp.dto;

import com.fitnessapp.model.*;
import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {


    private Double  weight;
    private Double  height;
    private Integer age;

    private GenderType gender;


    private Integer activityLevelId;
    private Integer dietTypeId;

    private MeatPreferenceType meatPreference;
    private Boolean consumesDairy;


    private Set<String> allergies;
    private Set<String> otherDietaryPreferences;

    private MealFrequencyPreferenceType mealFrequencyPreference;
}