// com.fitnessapp.dto.UserProfileDTO.java

package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set; // Запазваме Set, както е във User entity

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    // Добавени за да може фронтенда да ги показва
    private String email;
    private String fullName;

    private Double  weight;
    private Double  height;
    private Integer age;

    // Променени от GenderType на String, за да са по-лесни за работа на фронтенда
    private String gender; // MALE, FEMALE

    // Променени от Integer ID на String name за по-лесно четене/избор на фронтенда
    private String activityLevel; // SEDENTARY, MODERATELY_ACTIVE и т.н.
    private String dietType;      // Балансирана, Веган, Кето и т.н.

    // Променени от MeatPreferenceType на String
    private String meatPreference; // CHICKEN, BEEF, ALL, NONE

    private Boolean consumesDairy;

    // Запазваме Set<String>
    private Set<String> allergies;
    private Set<String> otherDietaryPreferences;

    // Променени от MealFrequencyPreferenceType на String
    private String mealFrequencyPreference; // TWO_TIMES_DAILY, FOUR_TIMES_DAILY и т.н.

    // Добавени полета от User entity
    private String trainingType;            // CARDIO, STRENGTH, HYBRID
    private Integer trainingDaysPerWeek;
    private Integer trainingDurationMinutes;
    private String level;                   // BEGINNER, INTERMEDIATE, ADVANCED

    // Добавено поле за цел
    private String goalName; // Отслабване, Натрупване на мускулна маса и т.н.
}