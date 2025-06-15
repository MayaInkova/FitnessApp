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
            System.out.println("Ролите са попълнени!");
        }


        if (dietTypeRepository.count() == 0) {
            dietTypeRepository.save(DietType.builder()
                    .name("Протеинова")
                    .description("Богата на протеини, ниска на въглехидрати.")
                    .build());
            dietTypeRepository.save(DietType.builder()
                    .name("Кето")
                    .description("Кетогенна диета с високо съдържание на мазнини и ниско съдържание на въглехидрати.").build());
            dietTypeRepository.save(DietType.builder()
                    .name("Балансирана").description("Балансирана диета, включваща всички хранителни групи.").build()); // Corrected from "Стандартна"
            dietTypeRepository.save(DietType.builder()
                    .name("Вегетарианска").description("Растителна диета, без месо, птици или риба.").build());
            dietTypeRepository.save(DietType.builder()
                    .name("Веган").description("Строго растителна диета, без животински продукти, включително млечни продукти и яйца.").build()); // Corrected from "Веганска"
            dietTypeRepository.save(DietType.builder() // Added "Палео"
                    .name("Палео")
                    .description("Фокусира се върху храни, достъпни за палеолитните хора (месо, риба, зеленчуци, плодове, ядки, семена), изключва зърнени култури, бобови растения, млечни продукти и преработени храни.")
                    .build());
            System.out.println("Типовете диети са попълнени!");
        }


        if (activityLevelRepository.count() == 0) {
            activityLevelRepository.save(ActivityLevel.builder()
                    .name("Заседнал").description("Малко или никакво упражнение").multiplier(1.2).build());
            activityLevelRepository.save(ActivityLevel.builder()
                    .name("Леко активен").description("Леки упражнения/спорт 1-3 дни/седмица").multiplier(1.375).build());
            activityLevelRepository.save(ActivityLevel.builder()
                    .name("Умерено активен").description("Умерени упражнения/спорт 3-5 дни/седмица").multiplier(1.55).build());
            activityLevelRepository.save(ActivityLevel.builder()
                    .name("Много активен").description("Интензивни упражнения/спорт 6-7 дни в седмицата").multiplier(1.725).build());
            activityLevelRepository.save(ActivityLevel.builder()
                    .name("Изключително активен").description("Много интензивни упражнения/физическа работа").multiplier(1.9).build());
            System.out.println("Нивата на активност са попълнени!");
        }


        if (goalRepository.count() == 0) {
            goalRepository.save(Goal.builder().name("Отслабване").description("Създайте калориен дефицит за отслабване.").calorieModifier(-500.0).build());
            goalRepository.save(Goal.builder().name("Наддаване на тегло").description("Създайте калориен излишък за наддаване на тегло.").calorieModifier(500.0).build());
            goalRepository.save(Goal.builder().name("Поддържане на тегло").description("Поддържане на текущото тегло.").calorieModifier(0.0).build());
            System.out.println("Целите са попълнени!");
        }


        if (recipeRepository.count() == 0) {

            DietType balancedDiet = dietTypeRepository.findByName("Балансирана").orElse(null);
            DietType vegetarianDiet = dietTypeRepository.findByName("Вегетарианска").orElse(null);
            DietType veganDiet = dietTypeRepository.findByName("Веган").orElse(null);
            DietType paleoDiet = dietTypeRepository.findByName("Палео").orElse(null);


            if (balancedDiet != null) {
                recipeRepository.save(Recipe.builder()
                        .name("Овесена каша с плодове")
                        .description("Здравословна закуска.")
                        .calories(300.0)
                        .protein(10.0)
                        .carbs(50.0)
                        .fat(8.0)
                        .isVegetarian(true)
                        .containsDairy(true)
                        .mealType(MealType.BREAKFAST)
                        .instructions("Сварете овесена каша, добавете плодове.")
                        .build());
            }
            if (veganDiet != null) {
                recipeRepository.save(Recipe.builder()
                        .name("Салата Киноа с авокадо")
                        .description("Лека и засищаща веган салата.")
                        .calories(450.0)
                        .protein(15.0)
                        .carbs(60.0)
                        .fat(20.0)
                        .isVegetarian(true)
                        .containsDairy(false)
                        .mealType(MealType.LUNCH)
                        .instructions("Сварете киноа, смесете с нарязани зеленчуци и авокадо.")
                        .build());
            }
            if (paleoDiet != null) {
                recipeRepository.save(Recipe.builder()
                        .name("Печено пиле със сладки картофи")
                        .description("Богато на протеини и комплексни въглехидрати.")
                        .calories(600.0)
                        .protein(45.0)
                        .carbs(40.0)
                        .fat(30.0)
                        .isVegetarian(false)
                        .containsDairy(false)
                        .meatType(MeatType.CHICKEN)
                        .mealType(MealType.DINNER)
                        .instructions("Изпечете пилешки бутчета със сладки картофи и подправки.")
                        .build());
            }

            System.out.println("Рецептите са попълнени!");
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

            System.out.println("Упражненията са попълнени!");
        }


        if (userRepository.count() == 0) {
            System.out.println("Потребителите са попълнени!");
        }
    }
}
