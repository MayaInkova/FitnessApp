package com.fitnessapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitnessapp.model.GenderType; // Уверете се, че импортирате правилните енуми
import com.fitnessapp.model.LevelType;
import com.fitnessapp.model.MeatPreferenceType;
import com.fitnessapp.model.MealFrequencyPreferenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

import java.util.Set; // Използваме Set за роли и алергии/предпочитания

@Data // Генерира getters, setters, toString, equals, hashCode
@NoArgsConstructor // Генерира конструктор без аргументи
@AllArgsConstructor // Генерира конструктор с всички аргументи
@Builder // Позволява използването на Builder модел за създаване на обекти
@JsonInclude(JsonInclude.Include.NON_NULL) // Включва само не-null полета при сериализация в JSON
public class UserResponseDTO {
    private Integer id; // Променено на Integer
    private String fullName;
    private String email;
    private Integer age;
    private Double height;
    private Double weight;
    private GenderType gender; // Може да върнете GenderType директно, ако Jackson може да го сериализира

    private Integer activityLevelId;
    private String activityLevelName; // Име на ActivityLevel

    private Integer goalId;
    private String goalName; // Име на Goal

    private Integer dietTypeId;
    private String dietTypeName; // Име на DietType

    private String trainingType; // String, ако TrainingType е Enum
    private Integer trainingDaysPerWeek;
    private Integer trainingDurationMinutes;
    private LevelType level; // LevelType

    private Set<String> allergies; // Set от String
    private MeatPreferenceType meatPreference; // MeatPreferenceType
    private Boolean consumesDairy;
    private MealFrequencyPreferenceType mealFrequencyPreference; // MealFrequencyPreferenceType
    private Set<String> otherDietaryPreferences; // Set от String

    private Set<String> roles; // Set от String имена на роли

     private Integer nutritionPlanId;
     private Integer trainingPlanId;
}