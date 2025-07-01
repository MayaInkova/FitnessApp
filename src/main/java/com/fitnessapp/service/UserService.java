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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.NoSuchElementException;

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


    // 🌟 МОДИФИЦИРАН МЕТОД: Сега приема опционален параметър за търсене
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsersForAdmin(String searchTerm) {
        logger.info("Извличане на всички потребителски DTO за администраторски изглед. SearchTerm: {}", searchTerm);
        List<User> users;

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            // Търсене по имейл или пълно име
            users = userRepository.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(searchTerm, searchTerm);
        } else {
            // Без търсене - връщаме всички
            users = userRepository.findAll();
        }

        return users.stream()
                .map(this::convertUserToUserResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public UserResponseDTO updateUserProfile(Integer userId, UserUpdateRequest updateRequest) {
        logger.info("Актуализиране на потребителски профил за ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Потребител не е намерен с ID: " + userId)); // Променено на NoSuchElementException за консистентност с другите методи

        Optional.ofNullable(updateRequest.getFullName()).ifPresent(user::setFullName);

        // 🌟 НОВО: Актуализиране на имейл с проверка за уникалност
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(updateRequest.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Имейлът '" + updateRequest.getEmail() + "' вече е зает.");
            }
            user.setEmail(updateRequest.getEmail());
        }

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
        return convertUserToUserResponseDTO(updatedUser);
    }

    @Transactional
    public void updateDietTypeForUser(Integer userId, String dietTypeName) {
        logger.info("Актуализиране на тип диета за потребител ID: {} до {}", userId, dietTypeName);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Потребител не е намерен с ID: " + userId));

        DietType dietType = dietTypeRepository.findByNameIgnoreCase(dietTypeName)
                .orElseThrow(() -> new NoSuchElementException("Тип диета '" + dietTypeName + "' не е намерен."));

        user.setDietType(dietType);
        userRepository.save(user);
        logger.info("Тип диета актуализиран за потребител ID: {}", userId);
    }

    @Transactional
    public User assignRole(Integer userId, String roleName) {
        logger.info("Присвояване на роля '{}' на потребител ID: {}", roleName, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Потребител с ID " + userId + " не е намерен."));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new NoSuchElementException("Роля '" + roleName + "' не е намерена."));

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        if (user.getRoles().contains(role)) {
            logger.warn("Потребител с ID {} вече има роля '{}'.", userId, roleName);
            throw new IllegalArgumentException("Потребител с ID " + userId + " вече има роля '" + roleName + "'.");
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
            throw new NoSuchElementException("Потребител с ID " + id + " не е намерен за изтриване."); // Променено на NoSuchElementException
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
        Hibernate.initialize(user.getRoles());

        Integer latestNutritionPlanId = user.getNutritionPlans() != null && !user.getNutritionPlans().isEmpty() ?
                user.getNutritionPlans().stream()
                        .max(Comparator.comparing(NutritionPlan::getDateGenerated))
                        .map(NutritionPlan::getId)
                        .orElse(null)
                : null;


        Integer latestTrainingPlanId = user.getTrainingPlans() != null && !user.getTrainingPlans().isEmpty() ?
                user.getTrainingPlans().stream()
                        .max(Comparator.comparing(TrainingPlan::getDateGenerated))
                        .map(TrainingPlan::getId)
                        .orElse(null)
                : null;


        Set<String> rolesNames = user.getRoles() != null ? user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()) : new HashSet<>();

        return UserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail()) // 🌟 Включваме имейла в DTO
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
                .trainingType(user.getTrainingType() != null ? user.getTrainingType().name() : null)
                .trainingDaysPerWeek(user.getTrainingDaysPerWeek())
                .trainingDurationMinutes(user.getTrainingDurationMinutes())
                .level(user.getLevel())
                .allergies(user.getAllergies() != null ? user.getAllergies() : new HashSet<>())
                .otherDietaryPreferences(user.getOtherDietaryPreferences() != null ? user.getOtherDietaryPreferences() : new HashSet<>())
                .meatPreference(user.getMeatPreference())
                .consumesDairy(user.getConsumesDairy())
                .mealFrequencyPreference(user.getMealFrequencyPreference())
                .roles(rolesNames)
                .nutritionPlanId(latestNutritionPlanId)
                .trainingPlanId(latestTrainingPlanId)
                .build();
    }
}