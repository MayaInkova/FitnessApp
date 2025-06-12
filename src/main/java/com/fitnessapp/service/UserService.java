package com.fitnessapp.service;

import com.fitnessapp.dto.UserUpdateRequest;
import com.fitnessapp.model.ActivityLevel;
import com.fitnessapp.model.DietType;
import com.fitnessapp.model.GenderType;
import com.fitnessapp.model.Goal;
import com.fitnessapp.model.MeatPreferenceType;
import com.fitnessapp.model.Role;
import com.fitnessapp.model.TrainingType;
import com.fitnessapp.model.User;
import com.fitnessapp.model.MealFrequencyPreferenceType;

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
    public User updateUserProfile(Integer userId, UserUpdateRequest updateRequest) { // КОРИГИРАНО: Име на метода
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Потребител не е намерен с ID: " + userId));

        // Актуализиране на основни полета
        Optional.ofNullable(updateRequest.getFullName()).ifPresent(user::setFullName);
        Optional.ofNullable(updateRequest.getAge()).ifPresent(user::setAge);
        Optional.ofNullable(updateRequest.getHeight()).ifPresent(user::setHeight);
        Optional.ofNullable(updateRequest.getWeight()).ifPresent(user::setWeight);
        Optional.ofNullable(updateRequest.getGender()).ifPresent(user::setGender);

        // Актуализиране на свързани Entity обекти по ID (ActivityLevel, Goal, DietType)
        Optional.ofNullable(updateRequest.getActivityLevelId())
                .flatMap(activityLevelRepository::findById)
                .ifPresent(user::setActivityLevel);

        Optional.ofNullable(updateRequest.getGoalId())
                .flatMap(goalRepository::findById)
                .ifPresent(user::setGoal);

        Optional.ofNullable(updateRequest.getDietTypeId())
                .flatMap(dietTypeRepository::findById)
                .ifPresent(user::setDietType);

        // Актуализиране на специфични предпочитания (вече са енуми)
        Optional.ofNullable(updateRequest.getTrainingType()).ifPresent(user::setTrainingType);
        Optional.ofNullable(updateRequest.getTrainingDaysPerWeek()).ifPresent(user::setTrainingDaysPerWeek);
        Optional.ofNullable(updateRequest.getTrainingDurationMinutes()).ifPresent(user::setTrainingDurationMinutes);
        Optional.ofNullable(updateRequest.getLevel()).ifPresent(user::setLevel);
        Optional.ofNullable(updateRequest.getMeatPreference()).ifPresent(user::setMeatPreference);
        Optional.ofNullable(updateRequest.getConsumesDairy()).ifPresent(user::setConsumesDairy);
        Optional.ofNullable(updateRequest.getMealFrequencyPreference()).ifPresent(user::setMealFrequencyPreference);

        // За колекции като allergies и otherDietaryPreferences, ако updateRequest ги съдържа, презаписваме ги
        if (updateRequest.getAllergies() != null) {
            user.setAllergies(updateRequest.getAllergies());
        }
        if (updateRequest.getOtherDietaryPreferences() != null) {
            user.setOtherDietaryPreferences(updateRequest.getOtherDietaryPreferences());
        }

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
