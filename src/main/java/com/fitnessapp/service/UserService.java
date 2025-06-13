package com.fitnessapp.service;

import com.fitnessapp.dto.UserUpdateRequest;
import com.fitnessapp.dto.UserResponseDTO; // Import the new DTO

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
import org.hibernate.Hibernate; // Import Hibernate for lazy loading initialization

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    /**
     * Saves a user entity to the database.
     * @param user The User entity to save.
     * @return The saved User entity.
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Retrieves a user entity by email.
     * This method is intended for internal service use where a User entity is required.
     * @param email The email of the user.
     * @return An Optional containing the User entity if found, otherwise empty.
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) { // Renamed from getUserByEmailDTO to return Entity
        return userRepository.findByEmail(email);
    }

    /**
     * Retrieves a user entity by ID.
     * This method is intended for internal service use where a User entity is required.
     * @param id The ID of the user.
     * @return The User entity if found, otherwise null. (Changed from Optional to match original)
     */
    @Transactional(readOnly = true)
    public User getUserById(Integer id) { // Re-added to return User entity
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Retrieves a user by email and converts it to a UserResponseDTO.
     * Initializes lazy-loaded collections before mapping to DTO.
     * This method is intended for API responses.
     * @param email The email of the user.
     * @return An Optional containing UserResponseDTO if found, otherwise empty.
     */
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserByEmailDTO(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertUserToUserResponseDTO);
    }

    /**
     * Retrieves a user by ID and converts it to a UserResponseDTO.
     * Initializes lazy-loaded collections before mapping to DTO.
     * This method is intended for API responses.
     * @param id The ID of the user.
     * @return An Optional containing UserResponseDTO if found, otherwise empty.
     */
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserByIdDTO(Integer id) {
        return userRepository.findById(id)
                .map(this::convertUserToUserResponseDTO);
    }



    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsersDTO() {
        return userRepository.findAll().stream()
                .map(this::convertUserToUserResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public UserResponseDTO updateUserProfile(Integer userId, UserUpdateRequest updateRequest) {
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
        return convertUserToUserResponseDTO(updatedUser); // Convert to DTO before returning
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

    private UserResponseDTO convertUserToUserResponseDTO(User user) {
        if (user == null) return null;

        Hibernate.initialize(user.getActivityLevel());
        Hibernate.initialize(user.getGoal());
        Hibernate.initialize(user.getDietType());
        Hibernate.initialize(user.getAllergies());
        Hibernate.initialize(user.getOtherDietaryPreferences());
        Hibernate.initialize(user.getNutritionPlan());
        Hibernate.initialize(user.getTrainingPlan());

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
                .trainingType(user.getTrainingType() != null ? user.getTrainingType().name() : null)
                .trainingDaysPerWeek(user.getTrainingDaysPerWeek())
                .trainingDurationMinutes(user.getTrainingDurationMinutes())
                .level(user.getLevel())
                .allergies(user.getAllergies())
                .otherDietaryPreferences(user.getOtherDietaryPreferences())
                .meatPreference(user.getMeatPreference())
                .consumesDairy(user.getConsumesDairy())
                .mealFrequencyPreference(user.getMealFrequencyPreference())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))

                .nutritionPlanId(user.getNutritionPlan() != null ? user.getNutritionPlan().getId() : null)
                .trainingPlanId(user.getTrainingPlan() != null ? user.getTrainingPlan().getId() : null)
                .build();
    }
}
