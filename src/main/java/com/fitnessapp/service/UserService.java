package com.fitnessapp.service;

import com.fitnessapp.dto.UserUpdateRequest;
import com.fitnessapp.dto.UserResponseDTO;

import com.fitnessapp.model.ActivityLevel;
import com.fitnessapp.model.DietType;
import com.fitnessapp.model.GenderType;
import com.fitnessapp.model.Goal;
import com.fitnessapp.model.MeatPreferenceType;
import com.fitnessapp.model.Role;
import com.fitnessapp.model.TrainingType;
import com.fitnessapp.model.User;
import com.fitnessapp.model.MealFrequencyPreferenceType;
import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.TrainingPlan;

import com.fitnessapp.repository.ActivityLevelRepository;
import com.fitnessapp.repository.DietTypeRepository;
import com.fitnessapp.repository.GoalRepository;
import com.fitnessapp.repository.RoleRepository;
import com.fitnessapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

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


    @Transactional
    public User saveUser(User user) {
        logger.info("Запазване на потребител: {}", user.getEmail());
        return userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        logger.debug("Опит за извличане на потребител по имейл: {}", email);
        return userRepository.findByEmail(email);
    }


    @Transactional(readOnly = true)
    public User getUserById(Integer id) {
        logger.debug("Опит за извличане на потребител по ID: {}", id);
        return userRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserByEmailDTO(String email) {
        logger.debug("Опит за извличане на потребителски DTO по имейл: {}", email);
        return userRepository.findByEmail(email)
                .map(this::convertUserToUserResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserByIdDTO(Integer id) {
        logger.debug("Опит за извличане на потребителски DTO по ID: {}", id);
        return userRepository.findById(id)
                .map(this::convertUserToUserResponseDTO);
    }


    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsersDTO() {
        logger.info("Извличане на всички потребителски DTO.");
        return userRepository.findAll().stream()
                .map(this::convertUserToUserResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public UserResponseDTO updateUserProfile(Integer userId, UserUpdateRequest updateRequest) {
        logger.info("Актуализиране на потребителски профил за ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Потребител не е намерен с ID: " + userId));

        Optional.ofNullable(updateRequest.getFullName()).ifPresent(user::setFullName);
        Optional.ofNullable(updateRequest.getAge()).ifPresent(user::setAge);
        Optional.ofNullable(updateRequest.getHeight()).ifPresent(user::setHeight);
        Optional.ofNullable(updateRequest.getWeight()).ifPresent(user::setWeight);
        Optional.ofNullable(updateRequest.getGender()).ifPresent(user::setGender);

        Optional.ofNullable(updateRequest.getActivityLevelId())
                .flatMap(activityLevelRepository::findById)
                .ifPresent(user::setActivityLevel);

        Optional.ofNullable(updateRequest.getGoalId())
                .flatMap(goalRepository::findById)
                .ifPresent(user::setGoal);

        Optional.ofNullable(updateRequest.getDietTypeId())
                .flatMap(dietTypeRepository::findById)
                .ifPresent(user::setDietType);

        Optional.ofNullable(updateRequest.getTrainingType()).ifPresent(user::setTrainingType);
        Optional.ofNullable(updateRequest.getTrainingDaysPerWeek()).ifPresent(user::setTrainingDaysPerWeek);
        Optional.ofNullable(updateRequest.getTrainingDurationMinutes()).ifPresent(user::setTrainingDurationMinutes);
        Optional.ofNullable(updateRequest.getLevel()).ifPresent(user::setLevel);
        Optional.ofNullable(updateRequest.getMeatPreference()).ifPresent(user::setMeatPreference);
        Optional.ofNullable(updateRequest.getConsumesDairy()).ifPresent(user::setConsumesDairy);
        Optional.ofNullable(updateRequest.getMealFrequencyPreference()).ifPresent(user::setMealFrequencyPreference);

        if (updateRequest.getAllergies() != null) {
            user.setAllergies(updateRequest.getAllergies());
        }
        if (updateRequest.getOtherDietaryPreferences() != null) {
            user.setOtherDietaryPreferences(updateRequest.getOtherDietaryPreferences());
        }

        User updatedUser = userRepository.save(user);
        logger.info("Потребителският профил е актуализиран за ID: {}", userId);
        return convertUserToUserResponseDTO(updatedUser); // Convert to DTO before returning
    }

    @Transactional
    public void updateDietTypeForUser(Integer userId, String dietTypeName) {
        logger.info("Актуализиране на тип диета за потребител ID: {} до {}", userId, dietTypeName);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Потребител не е намерен с ID: " + userId));

        DietType dietType = dietTypeRepository.findByNameIgnoreCase(dietTypeName)
                .orElseThrow(() -> new RuntimeException("Тип диета '" + dietTypeName + "' не е намерен."));

        user.setDietType(dietType);
        userRepository.save(user);
        logger.info("Тип диета актуализиран за потребител ID: {}", userId);
    }

    @Transactional
    public User assignRole(Integer userId, String roleName) {
        logger.info("Присвояване на роля '{}' на потребител ID: {}", roleName, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Потребител не е намерен с ID: " + userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Роля '" + roleName + "' не е намерена."));

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().add(role);

        User updatedUser = userRepository.save(user);
        logger.info("Роля присвоена на потребител ID: {}", userId);
        return updatedUser;
    }


    @Transactional
    public void deleteUser(Integer id) {
        logger.info("Опит за изтриване на потребител с ID: {}", id);
        if (!userRepository.existsById(id)) {
            logger.warn("Потребител с ID {} не е намерен за изтриване.", id);
            throw new RuntimeException("Потребител с ID " + id + " не е намерен за изтриване.");
        }
        userRepository.deleteById(id);
        logger.info("Потребител с ID {} успешно изтрит.", id);
    }



    private UserResponseDTO convertUserToUserResponseDTO(User user) {
        if (user == null) {
            logger.warn("Опит за конвертиране на празен потребителски обект в DTO.");
            return null;
        }


        Hibernate.initialize(user.getActivityLevel());
        Hibernate.initialize(user.getGoal());
        Hibernate.initialize(user.getDietType());
        Hibernate.initialize(user.getAllergies());
        Hibernate.initialize(user.getOtherDietaryPreferences());
        Hibernate.initialize(user.getNutritionPlans());
        Hibernate.initialize(user.getTrainingPlans());

        // Взимане на ID на най-новия хранителен план
        Integer latestNutritionPlanId = user.getNutritionPlans() != null && !user.getNutritionPlans().isEmpty() ?
                user.getNutritionPlans().stream()
                        .max(Comparator.comparing(NutritionPlan::getDateGenerated))
                        .map(NutritionPlan::getId)
                        .orElse(null)
                : null;

        // Взимане на ID на най-новия тренировъчен план
        Integer latestTrainingPlanId = user.getTrainingPlans() != null && !user.getTrainingPlans().isEmpty() ?
                user.getTrainingPlans().stream()
                        .max(Comparator.comparing(TrainingPlan::getDateGenerated))
                        .map(TrainingPlan::getId)
                        .orElse(null)
                : null;


        return UserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .age(user.getAge())
                .height(user.getHeight())
                .weight(user.getWeight())
                .gender(user.getGender())
                .activityLevelId(user.getActivityLevel() != null ? user.getActivityLevel().getId() : null)
                .activityLevelName(user.getActivityLevel() != null ? user.getActivityLevel().getName() : null)
                .goalId(user.getGoal() != null ? user.getGoal().getId() : null)
                .goalName(user.getGoal() != null ? user.getGoal().getName() : null)
                .dietTypeId(user.getDietType() != null ? user.getDietType().getId() : null)
                .dietTypeName(user.getDietType() != null ? user.getDietType().getName() : null)
                .trainingType(user.getTrainingType() != null ? user.getTrainingType().name() : null) // Assuming DTO expects String
                .trainingDaysPerWeek(user.getTrainingDaysPerWeek())
                .trainingDurationMinutes(user.getTrainingDurationMinutes())
                .level(user.getLevel())
                .allergies(user.getAllergies())
                .otherDietaryPreferences(user.getOtherDietaryPreferences())
                .meatPreference(user.getMeatPreference()) // MeatPreferenceType enum
                .consumesDairy(user.getConsumesDairy())
                .mealFrequencyPreference(user.getMealFrequencyPreference()) // MealFrequencyPreferenceType enum
                .roles(user.getRoles() != null ? user.getRoles().stream() // Roles are EAGER, should not be null in practice
                        .map(Role::getName)
                        .collect(Collectors.toSet()) : new HashSet<>())
                .nutritionPlanId(latestNutritionPlanId)
                .trainingPlanId(latestTrainingPlanId)
                .build();
    }

}