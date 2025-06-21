package com.fitnessapp.dto;

import com.fitnessapp.model.*;
import lombok.*;

import java.util.Set;

/**
 * Snapshot на профила, който потребителят редактира в страницата “Профил”.
 * Използваме го и в UserProfileMapper, за да синхронизираме Entity ⇄ DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    /* ───── базови данни ───── */
    private Double  weight;   // кг
    private Double  height;   // см
    private Integer age;

    private GenderType gender;

    /* ───── активности / диета ───── */
    private Integer activityLevelId;   // FK към ActivityLevel
    private Integer dietTypeId;        // FK към DietType

    private MeatPreferenceType meatPreference;
    private Boolean consumesDairy;

    /* ───── предпочитания / алергии ───── */
    private Set<String> allergies;                  // алергени
    private Set<String> otherDietaryPreferences;    // custom предпочитания

    private MealFrequencyPreferenceType mealFrequencyPreference;
}