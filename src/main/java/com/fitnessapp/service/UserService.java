package com.fitnessapp.service;

import com.fitnessapp.dto.UserProfileUpdateDto;
import com.fitnessapp.model.ActivityLevel;
import com.fitnessapp.model.GenderType;
import com.fitnessapp.model.Goal;
import com.fitnessapp.model.MeatPreferenceType;
import com.fitnessapp.model.Role;
import com.fitnessapp.model.TrainingType;
import com.fitnessapp.model.User;
import com.fitnessapp.model.DietType;
import com.fitnessapp.model.MealFrequencyPreferenceType; // УВЕРЕТЕ СЕ, ЧЕ ТОЗИ ИМПОРТ Е НАЛИЦЕ!
import com.fitnessapp.model.LevelType; // Уверете се, че този импорт е налице

import com.fitnessapp.repository.ActivityLevelRepository;
import com.fitnessapp.repository.DietTypeRepository;
import com.fitnessapp.repository.GoalRepository;
import com.fitnessapp.repository.RoleRepository;
import com.fitnessapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DietTypeRepository dietTypeRepository;
    private final ActivityLevelRepository activityLevelRepository;
    private final GoalRepository goalRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       DietTypeRepository dietTypeRepository,
                       ActivityLevelRepository activityLevelRepository,
                       GoalRepository goalRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.dietTypeRepository = dietTypeRepository;
        this.activityLevelRepository = activityLevelRepository;
        this.goalRepository = goalRepository;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUserProfile(Integer userId, UserProfileUpdateDto userData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Потребител не е намерен с ID: " + userId));

        Optional.ofNullable(userData.getFullName()).ifPresent(user::setFullName);
        Optional.ofNullable(userData.getAge()).ifPresent(user::setAge);
        Optional.ofNullable(userData.getHeight()).ifPresent(user::setHeight);
        Optional.ofNullable(userData.getWeight()).ifPresent(user::setWeight);
        Optional.ofNullable(userData.getAllergies()).ifPresent(user::setAllergies);

        if (userData.getGender() != null) {
            try {
                GenderType genderType = GenderType.fromString(userData.getGender());
                user.setGender(genderType);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Невалидна стойност за пол: " + userData.getGender() + ". Очаква се 'мъж' или 'жена'.", e);
            }
        }

        if (userData.getGoalName() != null) {
            Goal goal = goalRepository.findByNameIgnoreCase(userData.getGoalName())
                    .orElseThrow(() -> new RuntimeException("Цел '" + userData.getGoalName() + "' не е намерена."));
            user.setGoal(goal);
        }

        if (userData.getActivityLevelName() != null) {
            ActivityLevel activityLevel = activityLevelRepository.findByNameIgnoreCase(userData.getActivityLevelName())
                    .orElseThrow(() -> new RuntimeException("Ниво на активност '" + userData.getActivityLevelName() + "' не е намерено."));
            user.setActivityLevel(activityLevel);
        }

        if (userData.getDietTypeName() != null) {
            DietType dietType = dietTypeRepository.findByNameIgnoreCase(userData.getDietTypeName())
                    .orElseThrow(() -> new RuntimeException("Тип диета '" + userData.getDietTypeName() + "' не е намерен."));
            user.setDietType(dietType);
        }

        Optional.ofNullable(userData.getConsumesDairy()).ifPresent(user::setConsumesDairy);

        if (userData.getMeatPreference() != null) {
            try {
                MeatPreferenceType meatPreferenceType = MeatPreferenceType.fromString(userData.getMeatPreference());
                user.setMeatPreference(meatPreferenceType);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Невалидна стойност за предпочитание за месо: " + userData.getMeatPreference() + ". Моля, изберете от 'пилешко', 'телешко', 'риба', 'свинско', 'агнешко', 'без месо', 'няма значение'.", e);
            }
        }

        if (userData.getTrainingType() != null) {
            try {
                TrainingType trainingType = TrainingType.fromString(userData.getTrainingType());
                user.setTrainingType(trainingType);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Невалидна стойност за тип тренировка: " + userData.getTrainingType() + ". Очаква се 'тежести', 'без тежести' или 'кардио'.", e);
            }
        }

        Optional.ofNullable(userData.getTrainingDaysPerWeek()).ifPresent(user::setTrainingDaysPerWeek);
        Optional.ofNullable(userData.getTrainingDurationMinutes()).ifPresent(user::setTrainingDurationMinutes);

        if (userData.getMealFrequencyPreference() != null) {
            try {
                MealFrequencyPreferenceType mealFrequencyPreference = MealFrequencyPreferenceType.fromString(userData.getMealFrequencyPreference());
                user.setMealFrequencyPreference(mealFrequencyPreference);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Невалидна стойност за предпочитание за честота на хранене: " + userData.getMealFrequencyPreference() + ".", e);
            }
        }

        Optional.ofNullable(userData.getLevel()).ifPresent(user::setLevel);


        return userRepository.save(user);
    }

    @Transactional
    public void updateDietTypeForUser(Integer userId, String dietTypeName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Потребител не е намерен с ID: " + userId));

        DietType dietType = dietTypeRepository.findByNameIgnoreCase(dietTypeName)
                .orElseThrow(() -> new RuntimeException("Тип диета '" + dietTypeName + "' не е намерен."));

        user.setDietType(dietType);
        userRepository.save(user);
    }

    @Transactional
    public User assignRole(Integer userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Потребител не е намерен с ID: " + userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Роля '" + roleName + "' не е намерена."));

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().add(role);

        return userRepository.save(user);
    }
}