package com.fitnessapp.service;

import com.fitnessapp.dto.*;
import com.fitnessapp.model.*;
import com.fitnessapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NutritionPlanService {

    private static final Logger logger = LoggerFactory.getLogger(NutritionPlanService.class);

    private final NutritionPlanRepository        nutritionPlanRepository;
    private final RecipeRepository               recipeRepository;
    private final TrainingPlanRepository         trainingPlanRepository;
    private final UserRepository                 userRepository;
    private final TrainingPlanService            trainingPlanService;
    private final WeeklyNutritionPlanRepository  weeklyNutritionPlanRepository;

    /* ------------------------------------------------------------------ */
    /*  ЕДИН ДНЕВЕН ПЛАН                                                  */
    /* ------------------------------------------------------------------ */
    @Transactional
    public NutritionPlanDTO generateAndSaveNutritionPlanForUserDTO(User user){
        logger.info("Генериране и запазване на хранителен план за потребител: {}", user.getFullName());
        validateUserProfileForPlanGeneration(user);

        double targetCalories = NutritionCalculator.calculateTDEE(user);
        if(user.getGoal()!=null && user.getGoal().getCalorieModifier()!=null){
            targetCalories += user.getGoal().getCalorieModifier();
        }
        final double P = 0.30, C = 0.45, F = 0.25;

        NutritionPlan entity = generateDailyNutritionPlan(
                user,
                LocalDate.now(),
                targetCalories,
                (targetCalories*P)/4.0,
                (targetCalories*C)/4.0,
                (targetCalories*F)/9.0,
                null);
        return convertNutritionPlanToDTO(entity);
    }

    /* ------------------------------------------------------------------ */
    /*  СЕДМИЧЕН ПЛАН – генерира 7 NutritionPlan записа и ги връзва       */
    /* ------------------------------------------------------------------ */
    @Transactional
    public WeeklyNutritionPlanDTO generateAndSaveWeeklyNutritionPlanForUserDTO(User user) {
        logger.info("Генериране на седмичен хранителен план за потребител: {}", user.getFullName());
        validateUserProfileForPlanGeneration(user);

        LocalDate startDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endDate   = startDate.plusDays(6);

        /* 1️⃣  Създаваме и пазим седмичния контейнер */
        WeeklyNutritionPlan newWeeklyPlan = WeeklyNutritionPlan.builder()
                .user(user)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        final WeeklyNutritionPlan weeklyPlan = weeklyNutritionPlanRepository.save(newWeeklyPlan); // final – effectively‑final

        /* ---- базови калории и проценти ---- */
        double baseCalories = NutritionCalculator.calculateTDEE(user);
        if (user.getGoal()!=null && user.getGoal().getCalorieModifier()!=null){
            baseCalories += user.getGoal().getCalorieModifier();
        }
        final double P = 0.30, C = 0.45, F = 0.25;

        Map<com.fitnessapp.model.DayOfWeek, NutritionPlanDTO> dailyDTOs = new LinkedHashMap<>();
        TrainingPlan lastTraining = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user)
                .stream().findFirst().orElse(null);

        for(int i=0;i<7;i++){
            LocalDate current = startDate.plusDays(i);
            com.fitnessapp.model.DayOfWeek dow = com.fitnessapp.model.DayOfWeek.valueOf(current.getDayOfWeek().name());

            boolean isTraining = lastTraining!=null &&
                    lastTraining.getTrainingSessions().stream().anyMatch(s->s.getDayOfWeek()==dow);
            double dayCalories = isTraining? baseCalories*1.10 : baseCalories;

            /* 2️⃣  Вземаме или създаваме дневния план и го вързваме към weeklyPlan */
            NutritionPlan dayPlan = nutritionPlanRepository.findByUserAndDateGenerated(user,current)
                    .map(existing->{
                        if(!weeklyPlan.equals(existing.getWeeklyNutritionPlan())){
                            existing.setWeeklyNutritionPlan(weeklyPlan);
                            nutritionPlanRepository.save(existing);
                        }
                        return existing;
                    }).orElseGet(() -> generateDailyNutritionPlan(
                            user,
                            current,
                            dayCalories,
                            (dayCalories*P)/4.0,
                            (dayCalories*C)/4.0,
                            (dayCalories*F)/9.0,
                            weeklyPlan));

            weeklyPlan.addDailyPlan(dayPlan);
            dailyDTOs.put(dow, convertNutritionPlanToDTO(dayPlan));
        }

        double totalCal  = dailyDTOs.values().stream().mapToDouble(NutritionPlanDTO::getTargetCalories).sum();
        double totalProt = dailyDTOs.values().stream().mapToDouble(NutritionPlanDTO::getProtein).sum();
        double totalCarb = dailyDTOs.values().stream().mapToDouble(NutritionPlanDTO::getCarbohydrates).sum();
        double totalFat  = dailyDTOs.values().stream().mapToDouble(NutritionPlanDTO::getFat).sum();

        return WeeklyNutritionPlanDTO.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .startDate(startDate)
                .endDate(endDate)
                .dailyPlans(dailyDTOs)
                .totalTargetCalories(totalCal)
                .totalProteinGrams(totalProt)
                .totalCarbsGrams(totalCarb)
                .totalFatGrams(totalFat)
                .build();
    }


    /**
     * Генерира и запазва един дневен хранителен план.
     * Този метод вече приема и връзка към WeeklyNutritionPlan, ако планът е част от седмичен.
     * @param user Потребителят.
     * @param date Датата на плана.
     * @param targetCalories Целеви калории.
     * @param targetProteinGrams Целеви протеини.
     * @param targetCarbsGrams Целеви въглехидрати.
     * @param targetFatGrams Целеви мазнини.
     * @param weeklyPlanEntity WeeklyNutritionPlan, към който принадлежи този дневен план (може да е null).
     * @return Запазеният NutritionPlan entity.
     */
    @Transactional
    private NutritionPlan generateDailyNutritionPlan(User user, LocalDate date, double targetCalories, double targetProteinGrams, double targetCarbsGrams, double targetFatGrams, WeeklyNutritionPlan weeklyPlanEntity){

        Optional<NutritionPlan> existing = nutritionPlanRepository.findByUserAndDateGenerated(user, date);
        if(existing.isPresent()){
            NutritionPlan plan = existing.get();
            // Ако съществуващ план е открит, но се генерира като част от НОВ седмичен план,
            // може да поискате да го актуализирате или да хвърлите грешка.
            // За простота, сега просто го връщаме, но ако weeklyPlanEntity не е null
            // и текущият план е свързан с друг weeklyPlan, може да искате да го актуализирате.
            if (weeklyPlanEntity != null && !weeklyPlanEntity.equals(plan.getWeeklyNutritionPlan())) {
                plan.setWeeklyNutritionPlan(weeklyPlanEntity); // Актуализираме връзката, ако е част от нов седмичен план
                nutritionPlanRepository.save(plan); // Запазваме актуализацията
            }
            initializePlanLazyFields(plan);
            logger.info("Existing nutrition plan found for user {} for {}. Returning existing plan.", user.getFullName(), date);
            return plan;
        }

        logger.info("Генериране на нов дневен план за {} (Цел: {} kcal) за потребител: {}", date, targetCalories, user.getEmail());
        logger.info("Целеви макроси за {}: Протеин {:.2f}g, Въглехидрати {:.2f}g, Мазнини {:.2f}g",
                user.getEmail(), targetProteinGrams, targetCarbsGrams, targetFatGrams);


        List<Recipe> filtered = filterRecipes(recipeRepository.findAll(), user);
        if(filtered.isEmpty()) {
            logger.error("Няма рецепти, отговарящи на критериите за потребител: {}", user.getEmail());
            throw new IllegalStateException("Няма рецепти, отговарящи на критериите на вашия профил. Моля, коригирайте предпочитанията си или изчакайте нови рецепти.");
        }


        Map<MealType,List<Recipe>> byType = filtered.stream().collect(Collectors.groupingBy(Recipe::getMealType));

        List<com.fitnessapp.model.Meal> meals = new ArrayList<>();

        Map<MealType, Double> mealCalorieDistribution = new HashMap<>();
        mealCalorieDistribution.put(MealType.BREAKFAST, 0.20);
        mealCalorieDistribution.put(MealType.LUNCH, 0.35);
        mealCalorieDistribution.put(MealType.DINNER, 0.35);
        mealCalorieDistribution.put(MealType.SNACK, 0.10); // Останалите 10% за закуски

        // Избор и добавяне на рецепти за основните хранения
        addMeal(meals, byType.getOrDefault(MealType.BREAKFAST, Collections.emptyList()), MealType.BREAKFAST, targetCalories * mealCalorieDistribution.get(MealType.BREAKFAST));
        addMeal(meals, byType.getOrDefault(MealType.LUNCH, Collections.emptyList()), MealType.LUNCH, targetCalories * mealCalorieDistribution.get(MealType.LUNCH));
        addMeal(meals, byType.getOrDefault(MealType.DINNER, Collections.emptyList()), MealType.DINNER, targetCalories * mealCalorieDistribution.get(MealType.DINNER));

        // Добавяне на закуски според предпочитанията за честота на хранене
        List<Recipe> snacks = byType.getOrDefault(MealType.SNACK, Collections.emptyList());
        double snackTargetCaloriesTotal = targetCalories * mealCalorieDistribution.get(MealType.SNACK);

        if (user.getMealFrequencyPreference() == MealFrequencyPreferenceType.FIVE_TIMES_DAILY) {
            if (!snacks.isEmpty()) {
                addMeal(meals, snacks, MealType.SNACK, snackTargetCaloriesTotal);
            }
        } else if (user.getMealFrequencyPreference() == MealFrequencyPreferenceType.SIX_TIMES_DAILY) {
            if (!snacks.isEmpty()) {
                // Първа закуска
                addMeal(meals, snacks, MealType.SNACK, snackTargetCaloriesTotal / 2);

                // Втора закуска - опит за различна рецепта
                List<Recipe> remainingSnacks = new ArrayList<>(snacks);
                // Премахваме току-що добавената рецепта, ако има такава
                if (!meals.isEmpty() && meals.get(meals.size() - 1).getMealType() == MealType.SNACK && meals.get(meals.size() - 1).getRecipe() != null) {
                    remainingSnacks.removeIf(r -> r.getId().equals(meals.get(meals.size() - 1).getRecipe().getId()));
                }
                if (!remainingSnacks.isEmpty()) {
                    addMeal(meals, remainingSnacks, MealType.SNACK, snackTargetCaloriesTotal / 2);
                } else if (snacks.size() > 0) {
                    // Ако няма други закуски, добавяме същата, но може да се помисли за по-добро управление
                    addMeal(meals, snacks, MealType.SNACK, snackTargetCaloriesTotal / 2);
                }
            }
        }

        NutritionPlan plan = new NutritionPlan();
        plan.setUser(user);
        plan.setTargetCalories(targetCalories);
        plan.setDateGenerated(date);
        plan.setWeeklyNutritionPlan(weeklyPlanEntity);

        // --- ПОПЪЛВАНЕ НА SNAPSHOT ПОЛЕТАТА ---
        plan.setUserGenderSnapshot(user.getGender());
        plan.setUserAgeSnapshot(user.getAge());
        plan.setUserWeightSnapshot(user.getWeight());
        plan.setUserHeightSnapshot(user.getHeight());
        plan.setUserActivityLevelSnapshot(user.getActivityLevel());
        plan.setUserDietTypeSnapshot(user.getDietType());
        plan.setUserAllergiesSnapshot(
                user.getAllergies() != null && !user.getAllergies().isEmpty() ?
                        String.join(", ", user.getAllergies()) : null
        );

        plan.setUserOtherDietaryPreferencesSnapshot(
                user.getOtherDietaryPreferences() != null && !user.getOtherDietaryPreferences().isEmpty() ?
                        String.join(", ", user.getOtherDietaryPreferences()) : null
        );
        plan.setUserMeatPreferenceSnapshot(user.getMeatPreference());
        plan.setUserConsumesDairySnapshot(user.getConsumesDairy());
        plan.setUserMealFrequencyPreferenceSnapshot(user.getMealFrequencyPreference());
        plan.setGoal(user.getGoal());

        // Задаваме meals към плана. Тъй като NutritionPlan има addMeal, използваме това
        // Но ако създаваме нов план и имаме колекция, може просто да я сетнем.
        // Ако вече имаме plan.setMeals(new ArrayList<>()), тогава добавянето е ОК.
        // Тъй като вие сте сложили @Builder.Default private List<Meal> meals = new ArrayList<>();
        // това е достатъчно.
        for (com.fitnessapp.model.Meal m : meals) {
            plan.addMeal(m); // Използваме addMeal, за да установим двупосочната връзка
        }


        // Изчисляваме реалните макроси и калории на базата на избраните и порционирани рецепти
        double protein = 0, fat = 0, carbs = 0;
        double actualCalories = 0;
        for(com.fitnessapp.model.Meal m: plan.getMeals()){ // Използваме plan.getMeals() след добавянето
            Recipe r = m.getRecipe();
            if(r!=null){
                double currentProtein = Optional.ofNullable(r.getProtein()).orElse(0.0);
                double currentFat = Optional.ofNullable(r.getFat()).orElse(0.0);
                double currentCarbs = Optional.ofNullable(r.getCarbs()).orElse(0.0);
                double currentCalories = Optional.ofNullable(r.getCalories()).orElse(0.0);

                protein += currentProtein * m.getPortionSize();
                fat     += currentFat * m.getPortionSize();
                carbs   += currentCarbs * m.getPortionSize();
                actualCalories += currentCalories * m.getPortionSize();
            }
        }
        plan.setProtein(protein);
        plan.setFat(fat);
        plan.setCarbohydrates(carbs);


        NutritionPlan saved = nutritionPlanRepository.save(plan);
        logger.info("NutritionPlan #{} запазен за {} на дата {}", saved.getId(), user.getEmail(), date);
        logger.info("Генериран дневен план за {} с общо калории: {:.2f} kcal, протеин: {:.2f}g, въглехидрати: {:.2f}g, мазнини: {:.2f}g",
                date, actualCalories, protein, carbs, fat);
        return saved;
    }


    /**
     * Записва даден NutritionPlan entity обект в базата данни.
     * @param plan NutritionPlan за записване.
     * @return DTO обект на записания план.
     */
    @Transactional
    public NutritionPlanDTO saveNutritionPlan(NutritionPlan plan) {
        logger.info("Записване на NutritionPlan (директно): {}", plan.getId());
        NutritionPlan savedPlan = nutritionPlanRepository.save(plan);
        // При директно запазване, ако планирате да го връщате като DTO веднага, инициализирайте полетата
        initializePlanLazyFields(savedPlan);
        return convertNutritionPlanToDTO(savedPlan);
    }

    /**
     * Извлича всички хранителни планове за даден потребител.
     * @param user Потребителят.
     * @return Списък от DTO обекти на хранителни планове, сортирани по дата (най-новите първи).
     */
    @Transactional(readOnly = true)
    public List<NutritionPlanDTO> getNutritionPlansByUserDTO(User user) {
        logger.info("Извличане на хранителни планове за потребител: {}", user.getEmail());
        List<NutritionPlan> plans = nutritionPlanRepository.findByUser(user);
        plans.sort(Comparator.comparing(NutritionPlan::getDateGenerated).reversed());
        return plans.stream()
                .map(this::convertNutritionPlanToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Извлича всички хранителни планове от базата данни.
     * @return Списък от DTO обекти на всички хранителни планове.
     */
    @Transactional(readOnly = true)
    public List<NutritionPlanDTO> getAllNutritionPlansDTO() {
        logger.info("Извличане на всички хранителни планове.");
        List<NutritionPlan> allPlans = nutritionPlanRepository.findAll();
        return allPlans.stream()
                .map(this::convertNutritionPlanToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Извлича историята на хранителните планове за даден потребител.
     * @param userId ID на потребителя.
     * @return Списък от DTO обекти за история на хранителни планове.
     */
    @Transactional(readOnly = true)
    public List<NutritionPlanHistoryDTO> getNutritionPlanHistory(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        List<NutritionPlan> plans = nutritionPlanRepository.findByUserOrderByDateGeneratedDesc(user);

        return plans.stream()
                .map(this::convertNutritionPlanToHistoryDTO) // Нов помощен метод
                .collect(Collectors.toList());
    }

    /**
     * Помощен метод за конвертиране на NutritionPlan entity в NutritionPlanHistoryDTO.
     * @param plan NutritionPlan entity.
     * @return NutritionPlanHistoryDTO обект.
     */
    private NutritionPlanHistoryDTO convertNutritionPlanToHistoryDTO(NutritionPlan plan) {
        if (plan == null) return null;

        // Инициализирайте всички Lazy-заредени обекти, които се използват в DTO
        // Този метод вече инициализира повечето snapshot обекти.
        // Ако имате други Lazy полета, които се мапват към DTO и не са покрити,
        // трябва да ги инициализирате тук или в initializePlanLazyFields.
        initializePlanLazyFields(plan); // Използваме съществуващия метод за инициализация

        return NutritionPlanHistoryDTO.builder()
                .id(plan.getId())
                .dateGenerated(plan.getDateGenerated())
                .targetCalories(plan.getTargetCalories())
                .protein(plan.getProtein())
                .fat(plan.getFat())
                .carbohydrates(plan.getCarbohydrates())
                .goalName(plan.getGoal() != null ? plan.getGoal().getName() : null)
                .userGenderSnapshot(plan.getUserGenderSnapshot() != null ? plan.getUserGenderSnapshot().name() : null)
                .userAgeSnapshot(plan.getUserAgeSnapshot())
                .userWeightSnapshot(plan.getUserWeightSnapshot())
                .userHeightSnapshot(plan.getUserHeightSnapshot())
                .userActivityLevelSnapshotName(plan.getUserActivityLevelSnapshot() != null ? plan.getUserActivityLevelSnapshot().getName() : null)
                .userDietTypeSnapshotName(plan.getUserDietTypeSnapshot() != null ? plan.getUserDietTypeSnapshot().getName() : null)
                .userAllergiesSnapshot(plan.getUserAllergiesSnapshot())
                .userOtherDietaryPreferencesSnapshot(plan.getUserOtherDietaryPreferencesSnapshot())
                .userMeatPreferenceSnapshot(plan.getUserMeatPreferenceSnapshot() != null ? plan.getUserMeatPreferenceSnapshot().name() : null)
                .userConsumesDairySnapshot(plan.getUserConsumesDairySnapshot())
                .userMealFrequencyPreferenceSnapshot(plan.getUserMealFrequencyPreferenceSnapshot() != null ? plan.getUserMealFrequencyPreferenceSnapshot().name() : null)
                .build();
    }


    /**
     * Конвертира NutritionPlan entity в NutritionPlanDTO.
     * Инициализира Lazy-зареждащи се полета преди конверсия.
     * @param plan NutritionPlan entity.
     * @return NutritionPlanDTO обект.
     */
    @Transactional(readOnly = true) // Може да се ползва readOnly = true тук, защото само четем и инициализираме
    private NutritionPlanDTO convertNutritionPlanToDTO(NutritionPlan plan){
        if(plan==null) return null;
        initializePlanLazyFields(plan); // Инициализиране на Lazy полета
        return NutritionPlanDTO.builder()
                .id(plan.getId())
                .dateGenerated(plan.getDateGenerated())
                .targetCalories(plan.getTargetCalories())
                .protein(plan.getProtein())
                .fat(plan.getFat())
                .carbohydrates(plan.getCarbohydrates())
                .goalName(plan.getGoal()!=null? plan.getGoal().getName():null)
                .userId(plan.getUser()!=null? plan.getUser().getId():null)
                .userEmail(plan.getUser()!=null? plan.getUser().getEmail():null)
                .meals(plan.getMeals()!=null? plan.getMeals().stream().map(this::convertMealToMealDTO).collect(Collectors.toList()): Collections.emptyList())
                .build();
    }

    /**
     * Конвертира Meal entity в MealDTO.
     * Изчислява калориите и макросите за конкретната порция на храненето.
     * @param meal Meal entity.
     * @return MealDTO обект.
     */
    private MealDTO convertMealToMealDTO(com.fitnessapp.model.Meal meal){
        if(meal==null) return null;

        RecipeDTO recipeDTO = convertRecipeToRecipeDTO(meal.getRecipe());

        // Изчисляване на калории и макроси за конкретната порция
        Double portionSize = Optional.ofNullable(meal.getPortionSize()).orElse(1.0); // Дефолтна порция 1.0
        Double recipeCalories = (meal.getRecipe() != null) ? Optional.ofNullable(meal.getRecipe().getCalories()).orElse(0.0) : 0.0;
        Double recipeProtein = (meal.getRecipe() != null) ? Optional.ofNullable(meal.getRecipe().getProtein()).orElse(0.0) : 0.0;
        Double recipeCarbs = (meal.getRecipe() != null) ? Optional.ofNullable(meal.getRecipe().getCarbs()).orElse(0.0) : 0.0;
        Double recipeFat = (meal.getRecipe() != null) ? Optional.ofNullable(meal.getRecipe().getFat()).orElse(0.0) : 0.0;


        return MealDTO.builder()
                .id(meal.getId())
                .mealType(meal.getMealType())
                .portionSize(portionSize)
                .recipe(recipeDTO)
                .calculatedCalories(recipeCalories * portionSize)
                .calculatedProtein(recipeProtein * portionSize)
                .calculatedCarbs(recipeCarbs * portionSize)
                .calculatedFat(recipeFat * portionSize)
                .build();
    }

    /**
     * Конвертира Recipe entity в RecipeDTO.
     * Инициализира Lazy-зареждащи се полета.
     * @param recipe Recipe entity.
     * @return RecipeDTO обект.
     */
    private RecipeDTO convertRecipeToRecipeDTO(Recipe recipe){
        if(recipe==null) return null;
        Hibernate.initialize(recipe.getDietType());
        if(recipe.getAllergens() != null) Hibernate.initialize(recipe.getAllergens()); // Проверка за null преди initialize
        if(recipe.getTags() != null) Hibernate.initialize(recipe.getTags()); // Проверка за null преди initialize

        return RecipeDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .description(recipe.getDescription())
                .imageUrl(recipe.getImageUrl())
                .calories(recipe.getCalories())
                .protein(recipe.getProtein())
                .carbs(recipe.getCarbs())
                .fat(recipe.getFat())
                .mealType(recipe.getMealType())
                .dietTypeName(recipe.getDietType()!=null? recipe.getDietType().getName():null)
                .isVegetarian(recipe.getIsVegetarian())
                .containsDairy(recipe.getContainsDairy())
                .containsNuts(recipe.getContainsNuts())
                .containsFish(recipe.getContainsFish())
                .containsPork(recipe.getContainsPork())
                .meatType(recipe.getMeatType()) // Няма нужда от тернарен оператор за енуми, ако вече е инициализиран
                .allergens(recipe.getAllergens()!=null? recipe.getAllergens() : Collections.emptySet())
                .tags(recipe.getTags()!=null? recipe.getTags() : Collections.emptySet())
                .build();
    }

    /**
     * Конвертира TrainingPlan entity в TrainingPlanDTO.
     * @param plan TrainingPlan entity.
     * @return TrainingPlanDTO обект.
     */
    private TrainingPlanDTO convertTrainingPlanToDTO(TrainingPlan plan){
        if(plan==null) return null;
        Hibernate.initialize(plan.getTrainingSessions());
        // Добавена инициализация на упражненията за всяка сесия, за да не са празни
        if (plan.getTrainingSessions() != null) {
            plan.getTrainingSessions().forEach(session -> Hibernate.initialize(session.getExercises()));
        }

        return TrainingPlanDTO.builder()
                .id(plan.getId())
                .dateGenerated(plan.getDateGenerated())
                .daysPerWeek(plan.getDaysPerWeek())
                .durationMinutes(plan.getDurationMinutes())
                .userId(plan.getUser()!=null? plan.getUser().getId():null)
                .userEmail(plan.getUser()!=null? plan.getUser().getEmail():null)
                .trainingSessions(plan.getTrainingSessions()!=null? plan.getTrainingSessions().stream().map(this::convertTrainingSessionToTrainingSessionDTO).collect(Collectors.toList()): Collections.emptyList())
                .build();
    }

    /**
     * Конвертира TrainingSession entity в TrainingSessionDTO.
     * @param session TrainingSession entity.
     * @return TrainingSessionDTO обект.
     */
    private TrainingSessionDTO convertTrainingSessionToTrainingSessionDTO(TrainingSession session){
        if(session==null) return null;
        Hibernate.initialize(session.getExercises());
        return TrainingSessionDTO.builder()
                .id(session.getId())
                .dayOfWeek(session.getDayOfWeek())
                .durationMinutes(session.getDurationMinutes())
                .exercises(session.getExercises()!=null? session.getExercises().stream().map(this::convertExerciseToExerciseDTO).collect(Collectors.toList()): Collections.emptyList())
                .build();
    }

    /**
     * Конвертира Exercise entity в ExerciseDTO.
     * @param ex Exercise entity.
     * @return ExerciseDTO обект.
     */
    private ExerciseDTO convertExerciseToExerciseDTO(Exercise ex){
        if(ex==null) return null;
        return ExerciseDTO.builder()
                .id(ex.getId())
                .name(ex.getName())
                .description(ex.getDescription())
                .sets(ex.getSets())
                .reps(ex.getReps())
                .durationMinutes(ex.getDurationMinutes())
                .type(ex.getType()) // Добавете тип, ако DTO го има
                .difficultyLevel(ex.getDifficultyLevel()) // Добавете трудност, ако DTO го има
                .equipment(ex.getEquipment()) // Добавете оборудване, ако DTO го има
                .build();
    }


    /**
     * Извлича пълния план (хранителен и тренировъчен) за даден потребител.
     * @param userId ID на потребителя.
     * @return FullPlanDTO обект, съдържащ информация за двата плана.
     */
    @Transactional(readOnly = true)
    public FullPlanDTO getFullPlanByUserId(Integer userId){
        User user = userRepository.findById(userId).orElse(null);
        if(user==null){
            logger.warn("User #{} not found", userId);
            return null;
        }
        // Взимаме последния хранителен план
        NutritionPlan nutrition = nutritionPlanRepository.findByUser(user).stream().max(Comparator.comparing(NutritionPlan::getDateGenerated)).orElse(null);
        // Взимаме последния тренировъчен план
        TrainingPlan  training  = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user).stream().findFirst().orElse(null);

        // Инициализирайте lazy полетата, преди да се опитате да ги конвертирате
        if (nutrition != null) {
            initializePlanLazyFields(nutrition);
        }
        if (training != null) {
            Hibernate.initialize(training.getTrainingSessions());
            if (training.getTrainingSessions() != null) {
                training.getTrainingSessions().forEach(session -> Hibernate.initialize(session.getExercises()));
            }
        }


        if(nutrition==null && training==null) return FullPlanDTO.builder().build(); // Ако няма нито един план

        return FullPlanDTO.builder()
                .nutritionPlanId(nutrition!=null? nutrition.getId():null)
                .targetCalories(nutrition!=null? nutrition.getTargetCalories():null)
                .protein(nutrition!=null? nutrition.getProtein():null)
                .fat(nutrition!=null? nutrition.getFat():null)
                .carbohydrates(nutrition!=null? nutrition.getCarbohydrates():null)
                .goalName(nutrition!=null && nutrition.getGoal()!=null? nutrition.getGoal().getName():null)
                .meals(nutrition!=null? nutrition.getMeals().stream().map(this::convertMealToMealDTO).collect(Collectors.toList()): Collections.emptyList()) // Използвайте Collections.emptyList() вместо null
                .trainingPlanId(training!=null? training.getId():null)
                .trainingPlanDescription(getTrainingSummary(user)) // Уверете се, че getTrainingSummary работи коректно
                .trainingDaysPerWeek(training!=null? training.getDaysPerWeek():null)
                .trainingDurationMinutes(training!=null? training.getDurationMinutes():null)
                .trainingSessions(training!=null? training.getTrainingSessions().stream().map(this::convertTrainingSessionToTrainingSessionDTO).collect(Collectors.toList()): Collections.emptyList()) // Използвайте Collections.emptyList() вместо null
                .build();
    }

    // Добавен помощен метод за резюме на тренировъчен план, който липсваше
    private String getTrainingSummary(User user) {
        // Можете да използвате trainingPlanService за да вземете последния тренировъчен план
        // и да генерирате описание.
        TrainingPlan latestTrainingPlan = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user)
                .stream().findFirst().orElse(null);

        if (latestTrainingPlan != null) {
            return String.format("Последен тренировъчен план: %d дни/седмица, %d минути/сесия.",
                    latestTrainingPlan.getDaysPerWeek(), latestTrainingPlan.getDurationMinutes());
        }
        return "Няма наличен тренировъчен план.";
    }


    /**
     * Инициализира Lazy-зареждащи се полета на NutritionPlan entity,
     * за да се избегнат LazyInitializationException при конверсия към DTO.
     * @param plan NutritionPlan entity.
     */
    private void initializePlanLazyFields(NutritionPlan plan){
        if (plan == null) return;
        Hibernate.initialize(plan.getMeals());
        if(plan.getMeals()!=null){
            plan.getMeals().forEach(m->{
                if(m.getRecipe()!=null){
                    Hibernate.initialize(m.getRecipe()); // Инициализирайте самата рецепта
                    Hibernate.initialize(m.getRecipe().getDietType());
                    if(m.getRecipe().getAllergens() != null) Hibernate.initialize(m.getRecipe().getAllergens());
                    if(m.getRecipe().getTags() != null) Hibernate.initialize(m.getRecipe().getTags());
                }
            });
        }
        Hibernate.initialize(plan.getGoal());
        Hibernate.initialize(plan.getUserActivityLevelSnapshot());
        Hibernate.initialize(plan.getUserDietTypeSnapshot());
        Hibernate.initialize(plan.getWeeklyNutritionPlan());
        if (plan.getWeeklyNutritionPlan() != null) {
            Hibernate.initialize(plan.getWeeklyNutritionPlan().getDailyPlans()); // Ако има DailyPlans, инициализирайте ги
        }
        // Можете да добавите и user snapshot, ако е Lazy:
        Hibernate.initialize(plan.getUser());
    }

    /**
     * Валидира потребителския профил за наличие на основни данни,
     * необходими за генериране на хранителен план.
     * @param user Потребителят за валидация.
     * @throws IllegalArgumentException Ако липсват необходими данни.
     */
    private void validateUserProfileForPlanGeneration(User user){
        List<String> missing = new ArrayList<>();
        if(user.getGender()==null)               missing.add("пол");
        if(user.getAge()==null)                  missing.add("възраст");
        if(user.getHeight()==null)               missing.add("ръст");
        if(user.getWeight()==null)               missing.add("тегло");
        if(user.getActivityLevel()==null)        missing.add("ниво на активност");
        if(user.getGoal()==null)                 missing.add("цел");
        if(user.getDietType()==null)             missing.add("диетичен тип");
        if(user.getMeatPreference()==null)       missing.add("предпочитания за месо"); // Важно за филтрирането!
        if(user.getConsumesDairy()==null)        missing.add("консумира млечни продукти"); // Важно за филтрирането!
        if(user.getMealFrequencyPreference()==null) missing.add("честота на хранене"); // Важно за филтрирането!

        if(!missing.isEmpty())
            throw new IllegalArgumentException("Липсват следните данни за генериране на хранителен план: "+String.join(", ", missing));
    }

    /**
     * Помощен метод за филтриране на рецепти въз основа на предпочитанията на потребителя.
     * @param allRecipes Всички налични рецепти.
     * @param user Потребителят, чиито предпочитания се използват.
     * @return Филтриран списък с рецепти.
     */
    private List<Recipe> filterRecipes(List<Recipe> allRecipes, User user) {
        return allRecipes.stream().filter(recipe -> {
            // Филтриране по диетичен тип
            if (user.getDietType() != null && recipe.getDietType() != null &&
                    !recipe.getDietType().equals(user.getDietType())) {
                return false;
            }

            // Филтриране по предпочитания за месо
            if (user.getMeatPreference() != null) {
                if (user.getMeatPreference() == MeatPreferenceType.VEGETARIAN && !Optional.ofNullable(recipe.getIsVegetarian()).orElse(false)) {
                    return false;
                }
                // АКО имате isVegan в Recipe, добавете логика:
                // if (user.getMeatPreference() == MeatPreferenceType.VEGAN && !Optional.ofNullable(recipe.getIsVegan()).orElse(false)) {
                //     return false;
                // }
                if (user.getMeatPreference() == MeatPreferenceType.NO_PORK && Optional.ofNullable(recipe.getContainsPork()).orElse(false)) {
                    return false;
                }
                // Логика за съвместимост между MeatPreferenceType и MealMeatType
                if (recipe.getMeatType() != null && user.getMeatPreference() != null) {
                    if (user.getMeatPreference() == MeatPreferenceType.VEGETARIAN && recipe.getMeatType() != MeatPreferenceType.NONE) {
                        return false;
                    }
                    if (user.getMeatPreference() == MeatPreferenceType.NO_FISH && Optional.ofNullable(recipe.getContainsFish()).orElse(false)) {
                        return false;
                    }
                    // Добавете други конкретни проверки според вашите MeatPreferenceType стойности
                }
            }


            // Филтриране по алергии (Allergens е Set<String>, Allergies е List<String>)
            if (user.getAllergies() != null && !user.getAllergies().isEmpty() && recipe.getAllergens() != null) {
                if (user.getAllergies().stream().anyMatch(userAllergy ->
                        recipe.getAllergens().stream().anyMatch(recipeAllergen ->
                                recipeAllergen.equalsIgnoreCase(userAllergy)))) {
                    return false; // Рецептата съдържа алерген, към който потребителят е алергичен
                }
            }

            // Филтриране по млечни продукти
            if (user.getConsumesDairy() != null && !user.getConsumesDairy() && Optional.ofNullable(recipe.getContainsDairy()).orElse(false)) {
                return false; // Потребителят не консумира млечни, а рецептата съдържа
            }

            // Филтриране по други диетични предпочитания (ако имате такива в Recipe)
            // if (user.getOtherDietaryPreferences() != null && !user.getOtherDietaryPreferences().isEmpty()) {
            //     // Имплементирайте логика за съпоставяне на тези предпочитания с тагове/характеристики на рецептите
            // }

            return true; // Рецептата отговаря на всички критерии
        }).collect(Collectors.toList());
    }


    /**
     * Помощен метод за избор на най-подходящата рецепта за даден тип хранене
     * и добавянето й към списъка с хранения с изчислена порция.
     * @param meals Списък с хранения за добавяне.
     * @param recipes Налични рецепти за избор.
     * @param mealType Тип хранене (закуска, обяд, вечеря, междинно).
     * @param targetCaloriesForMeal Целеви калории за това хранене.
     */
    private void addMeal(List<com.fitnessapp.model.Meal> meals, List<Recipe> recipes, MealType mealType, double targetCaloriesForMeal) {
        if (recipes == null || recipes.isEmpty()) {
            logger.warn("Няма налични рецепти за {}. Пропускане.", mealType);
            return;
        }

        Recipe bestRecipe = null;
        double minCalorieDifference = Double.MAX_VALUE;

        // Намиране на най-близката рецепта по калории
        for (Recipe recipe : recipes) {
            double recipeCaloriesPerPortion = Optional.ofNullable(recipe.getCalories()).orElse(0.0);
            if (recipeCaloriesPerPortion > 0) { // Избягваме деление на нула
                double difference = Math.abs(recipeCaloriesPerPortion - targetCaloriesForMeal);
                if (difference < minCalorieDifference) {
                    minCalorieDifference = difference;
                    bestRecipe = recipe;
                }
            }
        }

        if (bestRecipe != null) {
            double recipeCaloriesPerPortion = Optional.ofNullable(bestRecipe.getCalories()).orElse(0.0);
            double portionSize = 1.0; // По подразбиране 1 порция

            if (recipeCaloriesPerPortion > 0) {
                // Изчисляваме размера на порцията, така че калориите да са близо до целевите
                portionSize = targetCaloriesForMeal / recipeCaloriesPerPortion;
                // Можете да ограничите portionSize до разумен диапазон (например 0.5 до 2.0)
                // portionSize = Math.max(0.5, Math.min(2.0, portionSize));
            }

            com.fitnessapp.model.Meal meal = com.fitnessapp.model.Meal.builder()
                    .mealType(mealType)
                    .recipe(bestRecipe)
                    .portionSize(portionSize)
                    .build();
            meals.add(meal);
            logger.debug("Добавено хранене: {} (Рецепта: '{}') с порция {:.2f} за целеви калории {:.2f}",
                    mealType, bestRecipe.getName(), portionSize, targetCaloriesForMeal);
        } else {
            logger.warn("Не беше намерена подходяща рецепта за {}. Моля, проверете наличните рецепти.", mealType);
        }
    }
}