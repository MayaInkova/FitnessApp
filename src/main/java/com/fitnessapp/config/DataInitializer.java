package com.fitnessapp.config;

import com.fitnessapp.model.*;
import com.fitnessapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RecipeRepository recipeRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DietTypeRepository dietTypeRepository;
    private final ActivityLevelRepository activityLevelRepository;
    private final GoalRepository goalRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(RecipeRepository recipeRepository,
                           ExerciseRepository exerciseRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           DietTypeRepository dietTypeRepository,
                           ActivityLevelRepository activityLevelRepository,
                           GoalRepository goalRepository,
                           PasswordEncoder passwordEncoder) {
        this.recipeRepository = recipeRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.dietTypeRepository = dietTypeRepository;
        this.activityLevelRepository = activityLevelRepository;
        this.goalRepository = goalRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void run(String... args) throws Exception {


        if (roleRepository.count() == 0) {
            roleRepository.save(Role.builder().name("ROLE_USER").build());
            roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
            roleRepository.save(Role.builder().name("ROLE_MODERATOR").build());
            System.out.println("Роли попълнени!");
        }


        if (dietTypeRepository.count() == 0) {
            dietTypeRepository.save(DietType.builder()
                    .name("Standard").description("Balanced diet including all food groups.").build());
            dietTypeRepository.save(DietType.builder()
                    .name("Vegetarian").description("Plant-based diet, no meat, poultry, or fish.").build());
            dietTypeRepository.save(DietType.builder()
                    .name("Vegan").description("Strictly plant-based, no animal products including dairy and eggs.").build());
            System.out.println("Типове диети попълнени!");
        }

        if (activityLevelRepository.count() == 0) {
            activityLevelRepository.save(ActivityLevel.builder()
                    .name("Sedentary").description("Little or no exercise").multiplier(1.2).build());
            activityLevelRepository.save(ActivityLevel.builder()
                    .name("Lightly Active").description("Light exercise/sports 1-3 days/week").multiplier(1.375).build());
            activityLevelRepository.save(ActivityLevel.builder()
                    .name("Moderately Active").description("Moderate exercise/sports 3-5 days/week").multiplier(1.55).build());
            activityLevelRepository.save(ActivityLevel.builder()
                    .name("Very Active").description("Hard exercise/sports 6-7 days a week").multiplier(1.725).build());
            activityLevelRepository.save(ActivityLevel.builder()
                    .name("Extra Active").description("Very hard exercise/physical job").multiplier(1.9).build());
            System.out.println("Нива на активност попълнени!");
        }

        if (goalRepository.count() == 0) {
            goalRepository.save(Goal.builder().name("Lose Weight").description("Create a calorie deficit to lose weight.").calorieModifier(-500.0).build());
            goalRepository.save(Goal.builder().name("Gain Weight").description("Create a calorie surplus to gain weight.").calorieModifier(500.0).build());
            goalRepository.save(Goal.builder().name("Maintain Weight").description("Maintain current weight.").calorieModifier(0.0).build());
            System.out.println("Цели попълнени!");
        }


        if (recipeRepository.count() == 0) {
            DietType std  = dietTypeRepository.findByName("Standard").orElse(null);
            DietType veg  = dietTypeRepository.findByName("Vegetarian").orElse(null);
            DietType vegan = dietTypeRepository.findByName("Vegan").orElse(null);


            System.out.println("Рецепти попълнени!");
        }

        if (exerciseRepository.count() == 0) {
            exerciseRepository.save(Exercise.builder()
                    .name("Клякания със собствено тегло")
                    .description("Упражнение за крака и глутеус.")
                    .sets(3).reps(15).durationMinutes(10)
                    .type(ExerciseType.BODYWEIGHT)
                    .difficultyLevel(DifficultyLevel.BEGINNER)
                    .equipment(EquipmentType.NONE)
                    .build());

            exerciseRepository.save(Exercise.builder()
                    .name("Лицеви опори")
                    .description("Упражнение за гърди и трицепс.")
                    .sets(3).reps(12).durationMinutes(8)
                    .type(ExerciseType.BODYWEIGHT)
                    .difficultyLevel(DifficultyLevel.BEGINNER)
                    .equipment(EquipmentType.NONE)
                    .build());

            exerciseRepository.save(Exercise.builder()
                    .name("Напади с дъмбели")
                    .description("Комбинира сила и баланс.")
                    .sets(3).reps(12).durationMinutes(8)
                    .type(ExerciseType.WEIGHTS)
                    .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                    .equipment(EquipmentType.DUMBBELLS)
                    .build());

            exerciseRepository.save(Exercise.builder()
                    .name("Мъртва тяга")
                    .description("Комплексно упражнение за цялото тяло.")
                    .sets(4).reps(8).durationMinutes(12)
                    .type(ExerciseType.WEIGHTS)
                    .difficultyLevel(DifficultyLevel.ADVANCED)
                    .equipment(EquipmentType.BARBELL)
                    .build());

            exerciseRepository.save(Exercise.builder()
                    .name("Бягане на пътека")
                    .description("Кардио за издръжливост.")
                    .durationMinutes(30)
                    .type(ExerciseType.CARDIO)
                    .difficultyLevel(DifficultyLevel.BEGINNER)
                    .equipment(EquipmentType.GYM_EQUIPMENT)
                    .build());

            System.out.println("Упражнения попълнени!");
        }


        if (userRepository.count() == 0) {
            System.out.println("Потребители попълнени!");
        }
    }
}