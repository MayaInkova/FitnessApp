package com.fitnessapp.config;

import com.fitnessapp.model.*;
import com.fitnessapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

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
    public void run(String... args) {
        seedRoles();
        seedDietTypes();
        seedActivityLevels();
        seedGoals();
        seedRecipes();
        seedExercises();
        seedTestUsers();
    }

    private void seedRoles() {
        if (roleRepository.count() > 0) return;
        roleRepository.save(Role.builder().name("ROLE_USER").build());
        roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        roleRepository.save(Role.builder().name("ROLE_MODERATOR").build());
        roleRepository.save(Role.builder().name("ROLE_GUEST").build());
        System.out.println("Ролите са попълнени!");
    }

    private void seedDietTypes() {
        if (dietTypeRepository.count() > 0) return;
        dietTypeRepository.save(DietType.builder().name("Протеинова").description("Богата на протеини, ниска на въглехидрати.").build());
        dietTypeRepository.save(DietType.builder().name("Кето").description("Кетогенна диета с високо съдържание на мазнини и ниско съдържание на въглехидрати.").build());
        dietTypeRepository.save(DietType.builder().name("Балансирана").description("Балансирана диета, включваща всички хранителни групи.").build());
        dietTypeRepository.save(DietType.builder().name("Вегетарианска").description("Растителна диета, без месо, птици или риба.").build());
        dietTypeRepository.save(DietType.builder().name("Веган").description("Строго растителна диета, без животински продукти, включително млечни продукти и яйца.").build());
        dietTypeRepository.save(DietType.builder().name("Палео").description("Фокусира се върху храни, достъпни за палеолитните хора (месо, риба, зеленчуци, плодове, ядки, семена), изключва зърнени култури, бобови растения, млечни продукти и преработени храни.").build());
        System.out.println("Типовете диети са попълнени!");
    }

    private void seedActivityLevels() {
        if (activityLevelRepository.count() > 0) return;
        activityLevelRepository.save(ActivityLevel.builder().name("Заседнал").description("Малко или никакво упражнение").multiplier(1.2).build());
        activityLevelRepository.save(ActivityLevel.builder().name("Леко активен").description("Леки упражнения/спорт 1-3 дни/седмица").multiplier(1.375).build());
        activityLevelRepository.save(ActivityLevel.builder().name("Умерено активен").description("Умерени упражнения/спорт 3-5 дни/седмица").multiplier(1.55).build());
        activityLevelRepository.save(ActivityLevel.builder().name("Много активен").description("Интензивни упражнения/спорт 6-7 дни в седмицата").multiplier(1.725).build());
        activityLevelRepository.save(ActivityLevel.builder().name("Изключително активен").description("Много интензивни упражнения/физическа работа").multiplier(1.9).build());
        System.out.println("Нивата на активност са попълнени!");
    }

    private void seedGoals() {
        if (goalRepository.count() > 0) return;
        goalRepository.save(Goal.builder().name("Отслабване").description("Създайте калориен дефицит за отслабване.").calorieModifier(-500.0).build());
        goalRepository.save(Goal.builder().name("Наддаване на тегло").description("Създайте калориен излишък за наддаване на тегло.").calorieModifier(500.0).build());
        goalRepository.save(Goal.builder().name("Поддържане на тегло").description("Поддържане на текущото тегло.").calorieModifier(0.0).build());
        System.out.println("Целите са попълнени!");
    }

    private void seedRecipes() {
        if (recipeRepository.count() > 0) return;

        DietType balanced = dietTypeRepository.findByName("Балансирана").orElse(null);
        DietType vegan = dietTypeRepository.findByName("Веган").orElse(null);
        DietType vegetarian = dietTypeRepository.findByName("Вегетарианска").orElse(null);
        DietType paleo = dietTypeRepository.findByName("Палео").orElse(null);
        DietType keto = dietTypeRepository.findByName("Кето").orElse(null);
        DietType protein = dietTypeRepository.findByName("Протеинова").orElse(null);




        recipeRepository.save(Recipe.builder()
                .name("Овесена каша с плодове и кисело мляко")
                .description("Здравословна закуска, богата на фибри.")
                .calories(350.0)
                .protein(12.0)
                .carbs(60.0)
                .fat(10.0)
                .isVegetarian(true)
                .containsDairy(true)
                .mealType(MealType.BREAKFAST)
                .instructions("Сварете овесена каша, добавете пресни плодове, мед и лъжица кисело мляко.")
                .dietType(balanced)
                .tags(new HashSet<>(Arrays.asList("бързо", "здравословно", "закуска")))
                .build());


        recipeRepository.save(Recipe.builder()
                .name("Пилешка супа с много зеленчуци")
                .description("Класическа пилешка супа, засищаща и питателна.")
                .calories(400.0)
                .protein(30.0)
                .carbs(30.0)
                .fat(15.0)
                .isVegetarian(false)
                .containsDairy(false)
                .meatType(MeatType.CHICKEN)
                .mealType(MealType.LUNCH)
                .instructions("Сварете пилешко месо, добавете моркови, картофи, целина и фиде.")
                .dietType(balanced)
                .tags(new HashSet<>(Arrays.asList("супа", "домашно", "обяд")))
                .build());


        recipeRepository.save(Recipe.builder()
                .name("Печена сьомга със задушени аспержи")
                .description("Лека и здравословна вечеря с омега-3.")
                .calories(550.0)
                .protein(40.0)
                .carbs(20.0)
                .fat(35.0)
                .isVegetarian(false)
                .containsDairy(false)
                .meatType(MeatType.FISH)
                .mealType(MealType.DINNER)
                .instructions("Изпечете сьомга във фурната, задушете аспержи с малко зехтин и чесън.")
                .dietType(balanced)
                .tags(new HashSet<>(Arrays.asList("риба", "здравословно", "бързо")))
                .build());




        recipeRepository.save(Recipe.builder()
                .name("Кето омлет с бекон и авокадо")
                .description("Засищаща кето закуска.")
                .calories(450.0)
                .protein(25.0)
                .carbs(5.0)
                .fat(35.0)
                .isVegetarian(false)
                .containsDairy(false) // За тези, които избягват млечни на кето
                .meatType(MeatType.PORK) // Бекон
                .mealType(MealType.BREAKFAST)
                .instructions("Изпържете бекон, след това омлет с яйца и авокадо.")
                .dietType(keto)
                .tags(new HashSet<>(Arrays.asList("кето", "бързо", "закуска")))
                .build());


        recipeRepository.save(Recipe.builder()
                .name("Кето телешки кюфтенца с гъбен сос")
                .description("Сочни телешки кюфтенца в кремообразен гъбен сос.")
                .calories(700.0)
                .protein(50.0)
                .carbs(10.0)
                .fat(50.0)
                .isVegetarian(false)
                .containsDairy(true) // Кремообразен сос може да съдържа млечни
                .meatType(MeatType.BEEF)
                .mealType(MealType.LUNCH)
                .instructions("Пригответе телешки кюфтенца, задушете гъби със сметана.")
                .dietType(keto)
                .tags(new HashSet<>(Arrays.asList("кето", "телешко", "богато")))
                .build());


        recipeRepository.save(Recipe.builder()
                .name("Кето пилешки крилца с бадемова коричка")
                .description("Хрупкави пилешки крилца, идеални за кето.")
                .calories(650.0)
                .protein(40.0)
                .carbs(8.0)
                .fat(50.0)
                .isVegetarian(false)
                .containsDairy(false)
                .containsNuts(true) // Съдържа ядки
                .meatType(MeatType.CHICKEN)
                .mealType(MealType.DINNER)
                .instructions("Оваляйте пилешки крилца в бадемово брашно и подправки, изпечете.")
                .dietType(keto)
                .allergens(new HashSet<>(Arrays.asList("ядки"))) // Алерген: Ядки
                .tags(new HashSet<>(Arrays.asList("кето", "пилешко", "бързо", "вечеря")))
                .build());



        recipeRepository.save(Recipe.builder()
                .name("Чиа пудинг с горски плодове")
                .description("Лека и хранителна веган закуска.")
                .calories(280.0)
                .protein(6.0)
                .carbs(40.0)
                .fat(12.0)
                .isVegetarian(true)
                .containsDairy(false)
                .mealType(MealType.BREAKFAST)
                .instructions("Смесете чиа семена с растително мляко и горски плодове, оставете да престои.")
                .dietType(vegan)
                .tags(new HashSet<>(Arrays.asList("веган", "закуска", "без млечни")))
                .build());


        recipeRepository.save(Recipe.builder()
                .name("Леща с моркови и целина")
                .description("Засищаща и богата на протеини веган супа.")
                .calories(380.0)
                .protein(20.0)
                .carbs(50.0)
                .fat(10.0)
                .isVegetarian(true)
                .containsDairy(false)
                .containsNuts(false)
                .mealType(MealType.LUNCH)
                .instructions("Сварете леща със зеленчуци и подправки.")
                .dietType(vegan)
                .tags(new HashSet<>(Arrays.asList("веган", "супа", "обяд")))
                .build());


        recipeRepository.save(Recipe.builder()
                .name("Тофу със сотирани зеленчуци и ориз")
                .description("Здравословна веган вечеря.")
                .calories(500.0)
                .protein(25.0)
                .carbs(60.0)
                .fat(18.0)
                .isVegetarian(true)
                .containsDairy(false)
                .mealType(MealType.DINNER)
                .instructions("Изпържете тофу, сотирайте зеленчуци и сервирайте с кафяв ориз.")
                .dietType(vegan)
                .allergens(new HashSet<>(Arrays.asList("соя"))) // Алерген: Соя
                .tags(new HashSet<>(Arrays.asList("веган", "азиатско", "вечеря")))
                .build());



        recipeRepository.save(Recipe.builder()
                .name("Палео Омлет с пилешко и зеленчуци")
                .description("Бърза и засищаща палео закуска.")
                .calories(350.0)
                .protein(25.0)
                .carbs(10.0)
                .fat(25.0)
                .isVegetarian(false)
                .containsDairy(false)
                .meatType(MeatType.CHICKEN)
                .mealType(MealType.BREAKFAST)
                .instructions("Разбийте яйца, добавете нарязано варено пилешко и зеленчуци, изпържете до готовност.")
                .dietType(paleo)
                .tags(new HashSet<>(Arrays.asList("палео", "закуска", "високопротеиново")))
                .build());

        recipeRepository.save(Recipe.builder()
                .name("Пилешка салата Палео със сладки картофи")
                .description("Свежа салата с пилешко филе и печени сладки картофи.")
                .calories(480.0)
                .protein(38.0)
                .carbs(30.0)
                .fat(25.0)
                .isVegetarian(false)
                .containsDairy(false)
                .meatType(MeatType.CHICKEN)
                .mealType(MealType.LUNCH)
                .instructions("Сварете или изпечете пилешко филе, накъсайте и смесете със зелени салати, авокадо, краставици и печени орехи. Овкусете със зехтин и лимон.")
                .dietType(paleo)
                .tags(new HashSet<>(Arrays.asList("палео", "салата", "обяд")))
                .build());

        recipeRepository.save(Recipe.builder()
                .name("Телешко с броколи и гъби")
                .description("Класическо палео ястие с много зеленчуци.")
                .calories(620.0)
                .protein(48.0)
                .carbs(25.0)
                .fat(38.0)
                .isVegetarian(false)
                .containsDairy(false)
                .meatType(MeatType.BEEF)
                .mealType(MealType.DINNER)
                .instructions("Задушете телешко месо с броколи и гъби в малко зехтин и подправки.")
                .dietType(paleo)
                .tags(new HashSet<>(Arrays.asList("палео", "телешко", "вечеря")))
                .build());



        recipeRepository.save(Recipe.builder()
                .name("Протеинов шейк с извара и горски плодове")
                .description("Бърза протеинова закуска или междинно хранене.")
                .calories(250.0)
                .protein(30.0)
                .carbs(20.0)
                .fat(8.0)
                .isVegetarian(true)
                .containsDairy(true)
                .mealType(MealType.BREAKFAST)
                .instructions("Смесете извара, протеин на прах, горски плодове и вода/мляко в блендер.")
                .dietType(protein)
                .tags(new HashSet<>(Arrays.asList("протеин", "бързо", "шейк")))
                .build());

        recipeRepository.save(Recipe.builder()
                .name("Грил пилешко филе с киноа и зелена салата")
                .description("Лека, но богата на протеини опция за обяд.")
                .calories(500.0)
                .protein(45.0)
                .carbs(40.0)
                .fat(18.0)
                .isVegetarian(false)
                .containsDairy(false)
                .meatType(MeatType.CHICKEN)
                .mealType(MealType.LUNCH)
                .instructions("Изпечете пилешко филе на грил, сварете киноа, сервирайте със зелена салата.")
                .dietType(protein)
                .tags(new HashSet<>(Arrays.asList("протеин", "пилешко", "обяд")))
                .build());


        recipeRepository.save(Recipe.builder()
                .name("Треска на фурна с лимон и билки")
                .description("Нискомаслена, богата на протеини вечеря.")
                .calories(400.0)
                .protein(35.0)
                .carbs(10.0)
                .fat(20.0)
                .isVegetarian(false)
                .containsDairy(false)
                .meatType(MeatType.FISH)
                .mealType(MealType.DINNER)
                .instructions("Изпечете филе от треска с резени лимон и пресни билки.")
                .dietType(protein)
                .tags(new HashSet<>(Arrays.asList("протеин", "риба", "лесно")))
                .build());




        recipeRepository.save(Recipe.builder()
                .name("Гръцко кисело мляко с мед и орехи")
                .description("Бърза вегетарианска закуска.")
                .calories(280.0)
                .protein(18.0)
                .carbs(25.0)
                .fat(12.0)
                .isVegetarian(true)
                .containsDairy(true)
                .containsNuts(true)
                .mealType(MealType.BREAKFAST)
                .instructions("Смесете гръцко кисело мляко с мед и нарязани орехи.")
                .dietType(vegetarian)
                .allergens(new HashSet<>(Arrays.asList("млечни", "ядки")))
                .tags(new HashSet<>(Arrays.asList("вегетарианска", "бързо", "закуска")))
                .build());

        recipeRepository.save(Recipe.builder()
                .name("Салата Капрезе с балсамов оцет")
                .description("Класическа вегетарианска салата.")
                .calories(320.0)
                .protein(10.0)
                .carbs(15.0)
                .fat(25.0)
                .isVegetarian(true)
                .containsDairy(false)
                .mealType(MealType.LUNCH)
                .instructions("Нарежете домати и моцарела, аранжирайте с босилек и полейте с балсамов оцет.")
                .dietType(vegetarian)
                .tags(new HashSet<>(Arrays.asList("вегетарианска", "салата", "обяд")))
                .build());


        System.out.println("Рецептите са попълнени!");
    }

    private void seedExercises() {
        if (exerciseRepository.count() > 0) return;

        exerciseRepository.save(Exercise.builder().name("Клякания със собствено тегло").description("Упражнение за крака и глутеус.").sets(3).reps(15).durationMinutes(10).type(ExerciseType.BODYWEIGHT).difficultyLevel(DifficultyLevel.BEGINNER).equipment(EquipmentType.NONE).build());
        exerciseRepository.save(Exercise.builder().name("Лицеви опори").description("Упражнение за гърди и трицепс.").sets(3).reps(12).durationMinutes(8).type(ExerciseType.BODYWEIGHT).difficultyLevel(DifficultyLevel.BEGINNER).equipment(EquipmentType.NONE).build());
        exerciseRepository.save(Exercise.builder().name("Планк").description("Укрепва коремните мускули и ядрото.").sets(3).reps(1).durationMinutes(5).type(ExerciseType.BODYWEIGHT).difficultyLevel(DifficultyLevel.BEGINNER).equipment(EquipmentType.NONE).build());
        exerciseRepository.save(Exercise.builder().name("Бърпи").description("Цялостно кардио и силово упражнение.").sets(3).reps(10).durationMinutes(10).type(ExerciseType.BODYWEIGHT).difficultyLevel(DifficultyLevel.INTERMEDIATE).equipment(EquipmentType.NONE).build());
        exerciseRepository.save(Exercise.builder().name("Напади със собствено тегло").description("Упражнение за крака и глутеус.").sets(3).reps(12).durationMinutes(8).type(ExerciseType.BODYWEIGHT).difficultyLevel(DifficultyLevel.BEGINNER).equipment(EquipmentType.NONE).build());

        exerciseRepository.save(Exercise.builder().name("Напади с дъмбели").description("Комбинира сила и баланс.").sets(3).reps(12).durationMinutes(8).type(ExerciseType.WEIGHTS).difficultyLevel(DifficultyLevel.INTERMEDIATE).equipment(EquipmentType.DUMBBELLS).build());
        exerciseRepository.save(Exercise.builder().name("Мъртва тяга").description("Комплексно упражнение за цялото тяло.").sets(4).reps(8).durationMinutes(12).type(ExerciseType.WEIGHTS).difficultyLevel(DifficultyLevel.ADVANCED).equipment(EquipmentType.BARBELL).build());
        exerciseRepository.save(Exercise.builder().name("Бенч преса").description("Упражнение за гърди с щанга.").sets(4).reps(10).durationMinutes(10).type(ExerciseType.WEIGHTS).difficultyLevel(DifficultyLevel.INTERMEDIATE).equipment(EquipmentType.BARBELL).build());
        exerciseRepository.save(Exercise.builder().name("Раменна преса с дъмбели").description("Упражнение за рамене.").sets(3).reps(10).durationMinutes(8).type(ExerciseType.WEIGHTS).difficultyLevel(DifficultyLevel.INTERMEDIATE).equipment(EquipmentType.DUMBBELLS).build());
        exerciseRepository.save(Exercise.builder().name("Гребане с щанга").description("Упражнение за гръб.").sets(3).reps(10).durationMinutes(10).type(ExerciseType.WEIGHTS).difficultyLevel(DifficultyLevel.ADVANCED).equipment(EquipmentType.BARBELL).build());
        exerciseRepository.save(Exercise.builder().name("Бицепсово сгъване с дъмбели").description("Упражнение за бицепс.").sets(3).reps(12).durationMinutes(6).type(ExerciseType.WEIGHTS).difficultyLevel(DifficultyLevel.BEGINNER).equipment(EquipmentType.DUMBBELLS).build());
        exerciseRepository.save(Exercise.builder().name("Трицепсово разгъване").description("Упражнение за трицепс.").sets(3).reps(12).durationMinutes(6).type(ExerciseType.WEIGHTS).difficultyLevel(DifficultyLevel.BEGINNER).equipment(EquipmentType.DUMBBELLS).build());



        exerciseRepository.save(Exercise.builder().name("Бягане на пътека").description("Кардио за издръжливост.").durationMinutes(30).type(ExerciseType.CARDIO).difficultyLevel(DifficultyLevel.BEGINNER).equipment(EquipmentType.GYM_EQUIPMENT).build());
        exerciseRepository.save(Exercise.builder().name("Колоездене (стационарно)").description("Кардио за крака.").durationMinutes(45).type(ExerciseType.CARDIO).difficultyLevel(DifficultyLevel.INTERMEDIATE).equipment(EquipmentType.GYM_EQUIPMENT).build());
        exerciseRepository.save(Exercise.builder().name("Скачане на въже").description("Високоинтензивно кардио.").durationMinutes(15).type(ExerciseType.CARDIO).difficultyLevel(DifficultyLevel.INTERMEDIATE).equipment(EquipmentType.NONE).build());
        exerciseRepository.save(Exercise.builder().name("Плуване").description("Цялостно кардио упражнение.").durationMinutes(40).type(ExerciseType.CARDIO).difficultyLevel(DifficultyLevel.ADVANCED).equipment(EquipmentType.NONE).build());


        exerciseRepository.save(Exercise.builder().name("Йога - Поздрав към Слънцето").description("Поредица от йога пози за гъвкавост и сила.").durationMinutes(20).type(ExerciseType.OTHER).difficultyLevel(DifficultyLevel.BEGINNER).equipment(EquipmentType.NONE).build());


        System.out.println("Упражненията са попълнени!");
    }

    private void seedTestUsers() {
        if (userRepository.count() > 0) return;
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RuntimeException("ROLE_USER не е намерен!"));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow(() -> new RuntimeException("ROLE_ADMIN не е намерен!"));

        if (userRepository.findByEmail("test@example.com").isEmpty()) {
            userRepository.save(User.builder().fullName("Тест Потребител").email("test@example.com").password(passwordEncoder.encode("password123")).roles(Set.of(userRole)).build());
        }
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            userRepository.save(User.builder().fullName("Админ Потребител").email("admin@example.com").password(passwordEncoder.encode("adminpass")).roles(Set.of(adminRole)).build());
        }
        System.out.println("Потребителите са попълнени!");
    }
}