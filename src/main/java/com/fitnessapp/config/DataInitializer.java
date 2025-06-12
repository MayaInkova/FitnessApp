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
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DietTypeRepository dietTypeRepository;
    private final ActivityLevelRepository activityLevelRepository;
    private final GoalRepository goalRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(RecipeRepository recipeRepository, UserRepository userRepository,
                           RoleRepository roleRepository, DietTypeRepository dietTypeRepository,
                           ActivityLevelRepository activityLevelRepository,
                           GoalRepository goalRepository, PasswordEncoder passwordEncoder) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.dietTypeRepository = dietTypeRepository;
        this.activityLevelRepository = activityLevelRepository;
        this.goalRepository = goalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Инициализация на Role
        if (roleRepository.count() == 0) {
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            roleRepository.save(userRole);

            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);

            Role moderatorRole = new Role();
            moderatorRole.setName("ROLE_MODERATOR");
            roleRepository.save(moderatorRole);

            System.out.println("Роли попълнени!");
        }

        // Инициализация на DietType
        if (dietTypeRepository.count() == 0) {
            DietType standardDiet = new DietType();
            standardDiet.setName("Standard");
            standardDiet.setDescription("Balanced diet including all food groups.");
            dietTypeRepository.save(standardDiet);

            DietType vegetarianDiet = new DietType();
            vegetarianDiet.setName("Vegetarian");
            vegetarianDiet.setDescription("Plant-based diet, no meat, poultry, or fish.");
            dietTypeRepository.save(vegetarianDiet);

            DietType veganDiet = new DietType();
            veganDiet.setName("Vegan");
            veganDiet.setDescription("Strictly plant-based, no animal products including dairy and eggs.");
            dietTypeRepository.save(veganDiet);

            System.out.println("Типове диети попълнени!");
        }

        // Инициализация на ActivityLevel
        if (activityLevelRepository.count() == 0) {
            ActivityLevel sedentary = new ActivityLevel();
            sedentary.setName("Sedentary");
            sedentary.setDescription("Little or no exercise");
            sedentary.setMultiplier(1.2);
            activityLevelRepository.save(sedentary);

            ActivityLevel lightActivity = new ActivityLevel();
            lightActivity.setName("Lightly Active");
            lightActivity.setDescription("Light exercise/sports 1-3 days/week");
            lightActivity.setMultiplier(1.375);
            activityLevelRepository.save(lightActivity);

            ActivityLevel moderateActivity = new ActivityLevel();
            moderateActivity.setName("Moderately Active");
            moderateActivity.setDescription("Moderate exercise/sports 3-5 days/week");
            moderateActivity.setMultiplier(1.55);
            activityLevelRepository.save(moderateActivity);

            ActivityLevel veryActive = new ActivityLevel();
            veryActive.setName("Very Active");
            veryActive.setDescription("Hard exercise/sports 6-7 days a week");
            veryActive.setMultiplier(1.725);
            activityLevelRepository.save(veryActive);

            ActivityLevel extraActive = new ActivityLevel();
            extraActive.setName("Extra Active");
            extraActive.setDescription("Very hard exercise/physical job");
            extraActive.setMultiplier(1.9);
            activityLevelRepository.save(extraActive);

            System.out.println("Нива на активност попълнени!");
        }

        // Инициализация на Goal
        if (goalRepository.count() == 0) {
            Goal loseWeightGoal = new Goal();
            loseWeightGoal.setName("Lose Weight");
            loseWeightGoal.setDescription("Create a calorie deficit to lose weight.");
            loseWeightGoal.setCalorieModifier(-500.0);
            goalRepository.save(loseWeightGoal);

            Goal gainWeightGoal = new Goal();
            gainWeightGoal.setName("Gain Weight");
            gainWeightGoal.setDescription("Create a calorie surplus to gain weight.");
            gainWeightGoal.setCalorieModifier(500.0);
            goalRepository.save(gainWeightGoal);

            Goal maintainGoal = new Goal();
            maintainGoal.setName("Maintain Weight");
            maintainGoal.setDescription("Maintain current weight.");
            maintainGoal.setCalorieModifier(0.0);
            goalRepository.save(maintainGoal);

            System.out.println("Цели попълнени!");
        }


        // Инициализация на Recipes
        if (recipeRepository.count() == 0) {
            DietType standardDiet = dietTypeRepository.findByName("Standard").orElse(null);
            DietType vegetarianDiet = dietTypeRepository.findByName("Vegetarian").orElse(null);
            DietType veganDiet = dietTypeRepository.findByName("Vegan").orElse(null);

            // Рецепти за закуска
            recipeRepository.save(Recipe.builder()
                    .name("Овесена каша с плодове")
                    .description("Здравословна закуска, богата на фибри.")
                    .imageUrl("oats.jpg")
                    .calories(350.0).protein(12.0).carbs(60.0).fat(8.0)
                    .tags(Set.of("здравословно", "закуска"))
                    .mealType(MealType.BREAKFAST)
                    .instructions("Сварете овесените ядки с вода или мляко. Добавете плодове по избор.")
                    .dietType(vegetarianDiet)
                    .allergens(Set.of("глутен"))
                    .meatType(MeatType.NONE)
                    .containsDairy(true)
                    .containsFish(false)
                    .containsNuts(false)
                    .containsPork(false)
                    .isVegetarian(true)
                    .build());

            recipeRepository.save(Recipe.builder()
                    .name("Яйца по панагюрски")
                    .description("Традиционна българска закуска с кисело мляко и яйца.")
                    .imageUrl("eggs_panagyurishte.jpg")
                    .calories(420.0).protein(25.0).carbs(15.0).fat(30.0)
                    .tags(Set.of("традиционно", "закуска"))
                    .mealType(MealType.BREAKFAST)
                    .instructions("Сварете яйцата поширани. Залейте с кисело мляко и запържен червен пипер.")
                    .dietType(standardDiet)
                    .allergens(Set.of("мляко", "яйца"))
                    .meatType(MeatType.NONE)
                    .containsDairy(true)
                    .containsFish(false)
                    .containsNuts(false)
                    .containsPork(false)
                    .isVegetarian(true)
                    .build());

            // Рецепти за обяд
            recipeRepository.save(Recipe.builder()
                    .name("Пилешка салата с киноа")
                    .description("Лека и протеинова салата за обяд.")
                    .imageUrl("chicken_quinoa_salad.jpg")
                    .calories(480.0).protein(35.0).carbs(40.0).fat(20.0)
                    .tags(Set.of("салата", "обяд", "протеин"))
                    .mealType(MealType.LUNCH)
                    .instructions("Сварете киноа, изпечете пилешко филе. Смесете със зеленчуци и дресинг.")
                    .dietType(standardDiet)
                    .allergens(Collections.emptySet())
                    .meatType(MeatType.CHICKEN)
                    .containsDairy(false)
                    .containsFish(false)
                    .containsNuts(false)
                    .containsPork(false)
                    .isVegetarian(false)
                    .build());

            recipeRepository.save(Recipe.builder()
                    .name("Леща по турски")
                    .description("Богата на протеини и фибри веган супа.")
                    .imageUrl("lentil_soup.jpg")
                    .calories(320.0).protein(18.0).carbs(50.0).fat(5.0)
                    .tags(Set.of("супа", "веган", "обяд"))
                    .mealType(MealType.LUNCH)
                    .instructions("Сварете леща със зеленчуци и подправки.")
                    .dietType(veganDiet)
                    .allergens(Collections.emptySet())
                    .meatType(MeatType.NONE)
                    .containsDairy(false)
                    .containsFish(false)
                    .containsNuts(false)
                    .containsPork(false)
                    .isVegetarian(true)
                    .build());

            // Рецепти за вечеря
            recipeRepository.save(Recipe.builder()
                    .name("Сьомга с аспержи и сладък картоф")
                    .description("Здравословна вечеря с омега-3 мастни киселини.")
                    .imageUrl("salmon_asparagus.jpg")
                    .calories(550.0).protein(40.0).carbs(30.0).fat(30.0)
                    .tags(Set.of("риба", "вечеря", "здравословно"))
                    .mealType(MealType.DINNER)
                    .instructions("Изпечете сьомгата, задушете аспержите и сладкия картоф.")
                    .dietType(standardDiet)
                    .allergens(Set.of("риба"))
                    .meatType(MeatType.FISH)
                    .containsDairy(false)
                    .containsFish(true)
                    .containsNuts(false)
                    .containsPork(false)
                    .isVegetarian(false)
                    .build());

            recipeRepository.save(Recipe.builder()
                    .name("Пиле с ориз")
                    .description("Класическа и засищаща вечеря.")
                    .imageUrl("chicken_rice.jpg")
                    .calories(600.0).protein(45.0).carbs(70.0).fat(15.0)
                    .tags(Set.of("класика", "вечеря"))
                    .mealType(MealType.DINNER)
                    .instructions("Сгответе пилешки късове с ориз и зеленчуци.")
                    .dietType(standardDiet)
                    .allergens(Collections.emptySet())
                    .meatType(MeatType.CHICKEN)
                    .containsDairy(false)
                    .containsFish(false)
                    .containsNuts(false)
                    .containsPork(false)
                    .isVegetarian(false)
                    .build());

            // Рецепти за междинни закуски (Snacks)
            recipeRepository.save(Recipe.builder()
                    .name("Протеинов шейк")
                    .description("Бърза и лесна закуска след тренировка.")
                    .imageUrl("protein_shake.jpg")
                    .calories(250.0).protein(30.0).carbs(20.0).fat(5.0)
                    .tags(Set.of("шейк", "следтренировка", "протеин"))
                    .mealType(MealType.SNACK)
                    .instructions("Смесете протеин на прах с мляко или вода и плодове.")
                    .dietType(standardDiet)
                    .allergens(Set.of("мляко"))
                    .meatType(MeatType.NONE)
                    .containsDairy(true)
                    .containsFish(false)
                    .containsNuts(false)
                    .containsPork(false)
                    .isVegetarian(true)
                    .build());

            recipeRepository.save(Recipe.builder()
                    .name("Ябълка с фъстъчено масло")
                    .description("Здравословна и засищаща междинна закуска.")
                    .imageUrl("apple_peanut_butter.jpg")
                    .calories(280.0).protein(8.0).carbs(30.0).fat(15.0)
                    .tags(Set.of("плодове", "ядки", "снак"))
                    .mealType(MealType.SNACK)
                    .instructions("Нарежете ябълка и намажете с фъстъчено масло.")
                    .dietType(vegetarianDiet)
                    .allergens(Set.of("фъстъци"))
                    .meatType(MeatType.NONE)
                    .containsDairy(false)
                    .containsFish(false)
                    .containsNuts(true)
                    .containsPork(false)
                    .isVegetarian(true)
                    .build());

            System.out.println("Рецепти попълнени!");
        }


        // Инициализация на Users
        if (userRepository.count() == 0) {
            Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
            Role moderatorRole = roleRepository.findByName("ROLE_MODERATOR").orElseThrow();

            DietType standardDiet = dietTypeRepository.findByName("Standard").orElseThrow();
            DietType vegetarianDiet = dietTypeRepository.findByName("Vegetarian").orElseThrow();
            DietType veganDiet = dietTypeRepository.findByName("Vegan").orElseThrow();

            ActivityLevel sedentaryLevel = activityLevelRepository.findByName("Sedentary").orElseThrow();
            ActivityLevel lightLevel = activityLevelRepository.findByName("Lightly Active").orElseThrow();
            ActivityLevel moderateLevel = activityLevelRepository.findByName("Moderately Active").orElseThrow();
            ActivityLevel veryActiveLevel = activityLevelRepository.findByName("Very Active").orElseThrow();
            ActivityLevel extraActiveLevel = activityLevelRepository.findByName("Extra Active").orElseThrow();

            Goal loseGoal = goalRepository.findByName("Lose Weight").orElseThrow();
            Goal gainGoal = goalRepository.findByName("Gain Weight").orElseThrow();
            Goal maintainGoal = goalRepository.findByName("Maintain Weight").orElseThrow();


            // Потребител с роля ADMIN
            userRepository.save(User.builder()
                    .fullName("Админ Администраторов")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("adminpass"))
                    .age(35).height(180.0).weight(80.0)
                    .gender(GenderType.MALE)
                    .activityLevel(veryActiveLevel)
                    .goal(maintainGoal)
                    .meatPreference(MeatPreferenceType.CHICKEN)
                    .consumesDairy(true)
                    .trainingType(TrainingType.WEIGHTS)
                    .allergies(Collections.emptySet())
                    .roles(Set.of(userRole, adminRole))
                    .dietType(standardDiet)
                    .trainingDaysPerWeek(4)
                    .trainingDurationMinutes(90)
                    .mealFrequencyPreference(MealFrequencyPreferenceType.THREE_TIMES_DAILY) // КОРЕКЦИЯ ТУК
                    .level(LevelType.ADVANCED)
                    .otherDietaryPreferences(Collections.emptySet())
                    .build());

            // Потребител с роля USER (вегетарианец)
            userRepository.save(User.builder()
                    .fullName("Мария Петрова")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("userpass"))
                    .age(25).height(165.0).weight(60.0)
                    .gender(GenderType.FEMALE)
                    .activityLevel(lightLevel)
                    .goal(loseGoal)
                    .meatPreference(MeatPreferenceType.NONE)
                    .consumesDairy(true)
                    .trainingType(TrainingType.BODYWEIGHT)
                    .allergies(Set.of("nuts"))
                    .roles(Set.of(userRole))
                    .dietType(vegetarianDiet)
                    .trainingDaysPerWeek(3)
                    .trainingDurationMinutes(45)
                    .mealFrequencyPreference(MealFrequencyPreferenceType.FIVE_TIMES_DAILY) // КОРЕКЦИЯ ТУК
                    .level(LevelType.BEGINNER)
                    .otherDietaryPreferences(Collections.emptySet())
                    .build());

            // Потребител с роля USER (веган)
            userRepository.save(User.builder()
                    .fullName("Иван Иванов")
                    .email("ivan@example.com")
                    .password(passwordEncoder.encode("ivanpass"))
                    .age(30).height(175.0).weight(75.0)
                    .gender(GenderType.MALE)
                    .activityLevel(moderateLevel)
                    .goal(gainGoal)
                    .meatPreference(MeatPreferenceType.NONE)
                    .consumesDairy(false)
                    .trainingType(TrainingType.WEIGHTS)
                    .allergies(Set.of("soy"))
                    .roles(Set.of(userRole))
                    .dietType(veganDiet)
                    .trainingDaysPerWeek(5)
                    .trainingDurationMinutes(75)
                    .mealFrequencyPreference(MealFrequencyPreferenceType.THREE_TIMES_DAILY) // КОРЕКЦИЯ ТУК - моля, прегледайте дали това е най-подходящата константа, ако имате друга за "3 основни + 2 закуски", добавете я в MealFrequencyPreferenceType enum.
                    .level(LevelType.ADVANCED)
                    .otherDietaryPreferences(Collections.emptySet())
                    .build());

            // Потребител с роля MODERATOR
            userRepository.save(User.builder()
                    .fullName("Модератор Модераторов")
                    .email("moderator@example.com")
                    .password(passwordEncoder.encode("modpass"))
                    .age(28).height(168.0).weight(65.0)
                    .gender(GenderType.FEMALE)
                    .activityLevel(moderateLevel)
                    .goal(maintainGoal)
                    .meatPreference(MeatPreferenceType.NO_PREFERENCE)
                    .consumesDairy(true)
                    .trainingType(TrainingType.CARDIO)
                    .allergies(Set.of("eggs"))
                    .roles(Set.of(userRole, moderatorRole))
                    .dietType(standardDiet)
                    .trainingDaysPerWeek(3)
                    .trainingDurationMinutes(60)
                    .mealFrequencyPreference(MealFrequencyPreferenceType.THREE_TIMES_DAILY) // КОРЕКЦИЯ ТУК
                    .level(LevelType.INTERMEDIATE)
                    .otherDietaryPreferences(Collections.emptySet())
                    .build());

            System.out.println("Потребители попълнени!");
        }
    }
}