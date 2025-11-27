package com.fitnessapp.mapper;


import com.fitnessapp.dto.UserProfileDTO;
import com.fitnessapp.model.*; // Импортирайте всички необходими Enum и Entity класове
import com.fitnessapp.repository.ActivityLevelRepository;
import com.fitnessapp.repository.DietTypeRepository;
import com.fitnessapp.repository.GoalRepository; // Добавяме GoalRepository
import org.springframework.beans.factory.annotation.Autowired; // За инжекция на репозиторите
import org.springframework.stereotype.Component;

import java.util.Optional; // За Optional

@Component
public class UserProfileMapper {

    // Тези репозиторита трябва да са @Autowired, за да могат да се използват
    @Autowired
    private ActivityLevelRepository activityLevelRepository;
    @Autowired
    private DietTypeRepository dietTypeRepository;
    @Autowired
    private GoalRepository goalRepository; // Инжектираме GoalRepository


    public UserProfileDTO toDto(User u){
        if (u == null) return null;

        return UserProfileDTO.builder()
                .email(u.getEmail()) // Добавено
                .fullName(u.getFullName()) // Добавено
                .weight(u.getWeight())
                .height(u.getHeight())
                .age(u.getAge())
                // Конвертираме Enum към String
                .gender(u.getGender() != null ? u.getGender().name() : null)
                // Конвертираме Entity към String name
                .activityLevel(u.getActivityLevel() != null ? u.getActivityLevel().getName() : null)
                .dietType(u.getDietType() != null ? u.getDietType().getName() : null)
                // Конвертираме Enum към String
                .meatPreference(u.getMeatPreference() != null ? u.getMeatPreference().name() : null)
                .consumesDairy(u.getConsumesDairy())
                // Set<String> се мапва директно
                .allergies(u.getAllergies())
                .otherDietaryPreferences(u.getOtherDietaryPreferences())
                // Конвертираме Enum към String
                .mealFrequencyPreference(u.getMealFrequencyPreference() != null ? u.getMealFrequencyPreference().name() : null)
                // Добавени полета от User entity
                .trainingType(u.getTrainingType() != null ? u.getTrainingType().name() : null)
                .trainingDaysPerWeek(u.getTrainingDaysPerWeek())
                .trainingDurationMinutes(u.getTrainingDurationMinutes())
                .level(u.getLevel() != null ? u.getLevel().name() : null)
                // Конвертираме Entity към String name
                .goalName(u.getGoal() != null ? u.getGoal().getName() : null)
                .build();
    }

    /** ъпдейтваме само полетата, които DTO-то носи (null → пропускаме) */
    public void updateEntity(User u, UserProfileDTO dto) {
        // Основни данни
        Optional.ofNullable(dto.getWeight()).ifPresent(u::setWeight);
        Optional.ofNullable(dto.getHeight()).ifPresent(u::setHeight);
        Optional.ofNullable(dto.getAge()).ifPresent(u::setAge);
        Optional.ofNullable(dto.getFullName()).ifPresent(u::setFullName);

        // Конвертиране от String към Enum GenderType
        if (dto.getGender() != null && !dto.getGender().isEmpty()) {
            u.setGender(GenderType.valueOf(dto.getGender()));
        } else if (dto.getGender() != null) { // Ако е изпратен празен String, задаваме null
            u.setGender(null);
        }

        // Конвертиране от String име към ActivityLevel Entity
        if (dto.getActivityLevel() != null && !dto.getActivityLevel().isEmpty()) {
            activityLevelRepository.findByName(dto.getActivityLevel())
                    .ifPresent(u::setActivityLevel);
        } else if (dto.getActivityLevel() != null) { // Ако е изпратен празен String, задаваме null
            u.setActivityLevel(null);
        }

        // Конвертиране от String име към DietType Entity
        if (dto.getDietType() != null && !dto.getDietType().isEmpty()) {
            dietTypeRepository.findByName(dto.getDietType())
                    .ifPresent(u::setDietType);
        } else if (dto.getDietType() != null) { // Ако е изпратен празен String, задаваме null
            u.setDietType(null);
        }

        // Конвертиране от String към Enum MeatPreferenceType
        if (dto.getMeatPreference() != null && !dto.getMeatPreference().isEmpty()) {
            u.setMeatPreference(MeatPreferenceType.valueOf(dto.getMeatPreference()));
        } else if (dto.getMeatPreference() != null) { // Ако е изпратен празен String, задаваме null
            u.setMeatPreference(null);
        }

        Optional.ofNullable(dto.getConsumesDairy()).ifPresent(u::setConsumesDairy);

        // Set<String> се мапват директно. Важно: Ако DTO изпрати null, Set-а ще стане null.
        // Ако искаме да се изчисти, но да не стане null, трябва да проверим за празен Set.
        if(dto.getAllergies()!=null) {
            u.setAllergies(dto.getAllergies());
        }
        if(dto.getOtherDietaryPreferences()!=null) {
            u.setOtherDietaryPreferences(dto.getOtherDietaryPreferences());
        }

        // Конвертиране от String към Enum MealFrequencyPreferenceType
        if (dto.getMealFrequencyPreference() != null && !dto.getMealFrequencyPreference().isEmpty()) {
            u.setMealFrequencyPreference(MealFrequencyPreferenceType.valueOf(dto.getMealFrequencyPreference()));
        } else if (dto.getMealFrequencyPreference() != null) { // Ако е изпратен празен String, задаваме null
            u.setMealFrequencyPreference(null);
        }

        // Добавени полета за тренировка
        if (dto.getTrainingType() != null && !dto.getTrainingType().isEmpty()) {
            u.setTrainingType(TrainingType.valueOf(dto.getTrainingType()));
        } else if (dto.getTrainingType() != null) {
            u.setTrainingType(null);
        }
        Optional.ofNullable(dto.getTrainingDaysPerWeek()).ifPresent(u::setTrainingDaysPerWeek);
        Optional.ofNullable(dto.getTrainingDurationMinutes()).ifPresent(u::setTrainingDurationMinutes);
        if (dto.getLevel() != null && !dto.getLevel().isEmpty()) {
            u.setLevel(LevelType.valueOf(dto.getLevel()));
        } else if (dto.getLevel() != null) {
            u.setLevel(null);
        }

        // Конвертиране от String име към Goal Entity
        if (dto.getGoalName() != null && !dto.getGoalName().isEmpty()) {
            goalRepository.findByName(dto.getGoalName())
                    .ifPresent(u::setGoal);
        } else if (dto.getGoalName() != null) { // Ако е изпратен празен String, задаваме null
            u.setGoal(null);
        }
    }
}