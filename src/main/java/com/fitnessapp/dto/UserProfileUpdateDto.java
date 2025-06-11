package com.fitnessapp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // Добавен Builder

import java.util.Set;
import com.fitnessapp.model.LevelType; // Добавен импорт за LevelType

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Добавен Builder
public class UserProfileUpdateDto {
    private String fullName;
    private String gender; // GenderType enum ще бъде преобразуван от/към String
    private Integer age;
    private Double height;
    private Double weight;
    private Set<String> allergies;
    private String goalName; // Goal Entity име
    private String activityLevelName; // ActivityLevel Entity име
    private String dietTypeName; // DietType Entity име
    private Boolean consumesDairy;
    private String meatPreference; // MeatPreferenceType enum ще бъде преобразуван от/към String
    private String trainingType; // TrainingType enum ще бъде преобразуван от/към String
    private String mealFrequencyPreference;
    private Integer trainingDaysPerWeek;   // Добавено поле
    private Integer trainingDurationMinutes; // Добавено поле
    private LevelType level;             // Добавено поле
}