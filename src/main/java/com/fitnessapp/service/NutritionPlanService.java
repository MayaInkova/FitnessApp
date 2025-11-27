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

    private final NutritionPlanRepository nutritionPlanRepository;
    private final RecipeRepository recipeRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final UserRepository userRepository;
    private final TrainingPlanService trainingPlanService;
    private final WeeklyNutritionPlanRepository weeklyNutritionPlanRepository;

    @Transactional
    public NutritionPlanDTO generateAndSaveNutritionPlanForUserDTO(User user){
        logger.info("Генериране и запазване на хранителен план за потребител: {}", user.getFullName());
        validateUserProfileForPlanGeneration(user);

        double targetCalories = NutritionCalculator.calculateTDEE(user);
        if(user.getGoal()!=null && user.getGoal().getCalorieModifier()!=null){
            targetCalories += user.getGoal().getCalorieModifier();
        }
        final double P = 0.30, C = 0.45, F = 0.25;

        // За дневен план, подаваме празни сетове, тъй като нямаме контекст на седмица
        NutritionPlan entity = generateDailyNutritionPlan(
                user,
                LocalDate.now(),
                targetCalories,
                (targetCalories*P)/4.0,
                (targetCalories*C)/4.0,
                (targetCalories*F)/9.0,
                null,
                new HashSet<>(), // usedRecipeIdsInWeek
                new HashSet<>()); // usedRecipeIdsInDay
        return convertNutritionPlanToDTO(entity);
    }

    @Transactional
    public WeeklyNutritionPlanDTO generateAndSaveWeeklyNutritionPlanForUserDTO(User user) {
        logger.info("Генериране на седмичен хранителен план за потребител: {}", user.getFullName());
        validateUserProfileForPlanGeneration(user);

        LocalDate startDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endDate   = startDate.plusDays(6);

        /* Създаваме и пазим седмичния контейнер */
        WeeklyNutritionPlan newWeeklyPlan = WeeklyNutritionPlan.builder()
                .user(user)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        final WeeklyNutritionPlan weeklyPlan = weeklyNutritionPlanRepository.save(newWeeklyPlan);

        /* ---- базови калории и проценти ---- */
        double baseCalories = NutritionCalculator.calculateTDEE(user);
        if (user.getGoal()!=null && user.getGoal().getCalorieModifier()!=null){
            baseCalories += user.getGoal().getCalorieModifier();
        }
        final double P = 0.30, C = 0.45, F = 0.25;

        Map<com.fitnessapp.model.DayOfWeek, NutritionPlanDTO> dailyDTOs = new LinkedHashMap<>();
        TrainingPlan lastTraining = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user)
                .stream().findFirst().orElse(null);

        // Извличаме всички рецепти веднъж и ги филтрираме
        List<Recipe> allFilteredRecipes = filterRecipes(recipeRepository.findAll(), user);
        if(allFilteredRecipes.isEmpty()) {
            logger.error("Няма рецепти, отговарящи на критериите за потребител: {}", user.getEmail());
            throw new IllegalStateException("Няма рецепти, отговарящи на критериите на вашия профил. Моля, коригирайте предпочитанията си или изчакайте нови рецепти.");
        }

        // Нов сет за проследяване на използвани рецепти в рамките на цялата седмица
        Set<Integer> usedRecipeIdsInWeek = new HashSet<>();

        for(int i=0;i<7;i++){
            LocalDate current = startDate.plusDays(i);
            com.fitnessapp.model.DayOfWeek dow = com.fitnessapp.model.DayOfWeek.valueOf(current.getDayOfWeek().name());

            boolean isTraining = lastTraining!=null &&
                    lastTraining.getTrainingSessions().stream().anyMatch(s->s.getDayOfWeek()==dow);
            double dayCalories = isTraining? baseCalories*1.10 : baseCalories;

            // Вземаме или създаваме дневния план и го вързваме към weeklyPlan
            // Тук подаваме usedRecipeIdsInWeek, за да може да се избягват повторения между дните
            NutritionPlan dayPlan = nutritionPlanRepository.findByUserAndDateGenerated(user,current)
                    .map(existing->{
                        if(existing.getWeeklyNutritionPlan() == null || !weeklyPlan.equals(existing.getWeeklyNutritionPlan())){
                            existing.setWeeklyNutritionPlan(weeklyPlan);
                            nutritionPlanRepository.save(existing);
                        }
                        initializePlanLazyFields(existing);

                        // Добавяме ID-тата на рецептите от съществуващия план към usedRecipeIdsInWeek
                        // Това е важно, ако съществуващ план за даден ден вече има избрани рецепти
                        existing.getMeals().forEach(meal -> usedRecipeIdsInWeek.add(meal.getRecipe().getId()));

                        logger.info("Existing nutrition plan found for user {} for {}. Returning existing plan.", user.getFullName(), current);
                        return existing;
                    }).orElseGet(() -> generateDailyNutritionPlan(
                            user,
                            current,
                            dayCalories,
                            (dayCalories*P)/4.0,
                            (dayCalories*C)/4.0,
                            (dayCalories*F)/9.0,
                            weeklyPlan,
                            usedRecipeIdsInWeek, // Подаваме usedRecipeIdsInWeek
                            new HashSet<>())); // Нов сет за всеки ден (usedRecipeIdsInDay)

            weeklyPlan.addDailyPlan(dayPlan); // Добавяме дневния план към седмичния
            dailyDTOs.put(dow, convertNutritionPlanToDTO(dayPlan));
        }

        double totalCal  = dailyDTOs.values().stream().mapToDouble(NutritionPlanDTO::getTargetCalories).sum();
        double totalProt = dailyDTOs.values().stream().mapToDouble(NutritionPlanDTO::getProtein).sum();
        double totalCarb = dailyDTOs.values().stream().mapToDouble(NutritionPlanDTO::getCarbohydrates).sum();
        double totalFat  = dailyDTOs.values().stream().mapToDouble(NutritionPlanDTO::getFat).sum();

        // За да се уверят, че ежедневните планове са инициализирани за DTO конверсията
        Hibernate.initialize(weeklyPlan.getDailyPlans());


        return WeeklyNutritionPlanDTO.builder()
                .id(weeklyPlan.getId())
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

    @Transactional
    private NutritionPlan generateDailyNutritionPlan(User user, LocalDate date, double targetCalories, double targetProteinGrams, double targetCarbsGrams, double targetFatGrams, WeeklyNutritionPlan weeklyPlanEntity, Set<Integer> usedRecipeIdsInWeek, Set<Integer> usedRecipeIdsInDay){

        Optional<NutritionPlan> existing = nutritionPlanRepository.findByUserAndDateGenerated(user, date);
        if(existing.isPresent()){
            NutritionPlan plan = existing.get();
            if (weeklyPlanEntity != null && (plan.getWeeklyNutritionPlan() == null || !weeklyPlanEntity.equals(plan.getWeeklyNutritionPlan()))) {
                plan.setWeeklyNutritionPlan(weeklyPlanEntity);
                nutritionPlanRepository.save(plan);
            }
            initializePlanLazyFields(plan);
            // Ако има съществуващ план, добавяме неговите рецепти към usedRecipeIdsInWeek
            plan.getMeals().forEach(meal -> usedRecipeIdsInWeek.add(meal.getRecipe().getId()));
            logger.info("Existing nutrition plan found for user {} for {}. Returning existing plan.", user.getFullName(), date);
            return plan;
        }

        logger.info("Генериране на нов дневен план за {} (Цел: {} kcal) за потребител: {}", date, targetCalories, user.getEmail());
        logger.info("Целеви макроси за {}: Протеин {:.2f}g, Въглехидрати {:.2f}g, Мазнини {:.2f}g",
                user.getEmail(), targetProteinGrams, targetCarbsGrams, targetFatGrams);


        List<Recipe> filteredRecipes = filterRecipes(recipeRepository.findAll(), user);
        if(filteredRecipes.isEmpty()) {
            logger.error("Няма рецепти, отговарящи на критериите за потребител: {}", user.getEmail());
            throw new IllegalStateException("Няма рецепти, отговарящи на критериите на вашия профил. Моля, коригирайте предпочитанията си или изчакайте нови рецепти.");
        }


        Map<MealType,List<Recipe>> byType = filteredRecipes.stream().collect(Collectors.groupingBy(Recipe::getMealType));

        List<com.fitnessapp.model.Meal> meals = new ArrayList<>();

        Map<MealType, Double> mealCalorieDistribution = new HashMap<>();
        mealCalorieDistribution.put(MealType.BREAKFAST, 0.20);
        mealCalorieDistribution.put(MealType.LUNCH, 0.35);
        mealCalorieDistribution.put(MealType.DINNER, 0.35);
        mealCalorieDistribution.put(MealType.SNACK, 0.10); // Снакът получава 10% от общите калории

        // Избор и добавяне на рецепти за основните хранения
        addMeal(meals, byType.getOrDefault(MealType.BREAKFAST, Collections.emptyList()), MealType.BREAKFAST, targetCalories * mealCalorieDistribution.get(MealType.BREAKFAST), usedRecipeIdsInWeek, usedRecipeIdsInDay);
        addMeal(meals, byType.getOrDefault(MealType.LUNCH, Collections.emptyList()), MealType.LUNCH, targetCalories * mealCalorieDistribution.get(MealType.LUNCH), usedRecipeIdsInWeek, usedRecipeIdsInDay);
        addMeal(meals, byType.getOrDefault(MealType.DINNER, Collections.emptyList()), MealType.DINNER, targetCalories * mealCalorieDistribution.get(MealType.DINNER), usedRecipeIdsInWeek, usedRecipeIdsInDay);

        // Добавяне на закуски според предпочитанията за честота на хранене
        List<Recipe> snacks = byType.getOrDefault(MealType.SNACK, Collections.emptyList());
        double snackTargetCaloriesTotal = targetCalories * mealCalorieDistribution.get(MealType.SNACK); // 10% от общите калории


        // Ако "4 хранения" означава 3 основни + 1 снак, тогава добавяме FOUR_TIMES_DAILY тук.
        if (user.getMealFrequencyPreference() == MealFrequencyPreferenceType.FIVE_TIMES_DAILY ||
                user.getMealFrequencyPreference() == MealFrequencyPreferenceType.FOUR_TIMES_DAILY) { // Добавен FOUR_TIMES_DAILY
            if (!snacks.isEmpty()) {
                addMeal(meals, snacks, MealType.SNACK, snackTargetCaloriesTotal, usedRecipeIdsInWeek, usedRecipeIdsInDay);
            } else {
                logger.warn("Няма налични рецепти за снак за потребител {} с честота на хранене {}. Снакът е пропуснат.", user.getEmail(), user.getMealFrequencyPreference());
            }
        } else if (user.getMealFrequencyPreference() == MealFrequencyPreferenceType.SIX_TIMES_DAILY) {
            if (!snacks.isEmpty()) {
                // Първа закуска
                addMeal(meals, snacks, MealType.SNACK, snackTargetCaloriesTotal / 2, usedRecipeIdsInWeek, usedRecipeIdsInDay);

                // Втора закуска - опит за различна рецепта
                // Създаваме копие на списъка със закуски, за да можем да модифицираме без да засягаме оригиналния
                List<Recipe> secondSnackRecipes = new ArrayList<>(snacks);

                // Премахваме току-що добавената рецепта (първата закуска), ако има такава, за да осигурим разнообразие
                if (!meals.isEmpty() && meals.get(meals.size() - 1).getMealType() == MealType.SNACK && meals.get(meals.size() - 1).getRecipe() != null) {
                    final Integer lastAddedRecipeId = meals.get(meals.size() - 1).getRecipe().getId();
                    secondSnackRecipes.removeIf(r -> r.getId().equals(lastAddedRecipeId));
                }
                // Проверяваме и спрямо използваните за седмицата/деня
                secondSnackRecipes.removeIf(r -> usedRecipeIdsInWeek.contains(r.getId()) || usedRecipeIdsInDay.contains(r.getId()));


                if (!secondSnackRecipes.isEmpty()) {
                    addMeal(meals, secondSnackRecipes, MealType.SNACK, snackTargetCaloriesTotal / 2, usedRecipeIdsInWeek, usedRecipeIdsInDay);
                } else if (snacks.size() > 0) {
                    // Ако няма други различни закуски, добавяме отново от оригиналния списък (може да се повтори)
                    logger.warn("Недостатъчно разнообразни рецепти за втора закуска. Повтаря се рецепта.");
                    addMeal(meals, snacks, MealType.SNACK, snackTargetCaloriesTotal / 2, usedRecipeIdsInWeek, usedRecipeIdsInDay);
                }
            } else {
                logger.warn("Няма налични рецепти за снак за потребител {} с честота на хранене {}. Снаковете са пропуснати.", user.getEmail(), user.getMealFrequencyPreference());
            }
        }



        NutritionPlan plan = new NutritionPlan();
        plan.setUser(user);
        plan.setDateGenerated(date);
        plan.setDayOfWeek(com.fitnessapp.model.DayOfWeek.valueOf(date.getDayOfWeek().name()));
        plan.setWeeklyNutritionPlan(weeklyPlanEntity);
        plan.setTargetCalories(targetCalories);
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

        for (com.fitnessapp.model.Meal m : meals) {
            plan.addMeal(m);
        }

        double protein = 0, fat = 0, carbs = 0;
        double actualCalories = 0;
        for(com.fitnessapp.model.Meal m: plan.getMeals()){
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

    @Transactional
    public NutritionPlanDTO saveNutritionPlan(NutritionPlan plan) {
        logger.info("Записване на NutritionPlan (директно): {}", plan.getId());
        NutritionPlan savedPlan = nutritionPlanRepository.save(plan);
        initializePlanLazyFields(savedPlan);
        return convertNutritionPlanToDTO(savedPlan);
    }

    @Transactional(readOnly = true)
    public List<NutritionPlanDTO> getNutritionPlansByUserDTO(User user) {
        logger.info("Извличане на хранителни планове за потребител: {}", user.getEmail());
        List<NutritionPlan> plans = nutritionPlanRepository.findByUser(user);
        plans.sort(Comparator.comparing(NutritionPlan::getDateGenerated).reversed());
        return plans.stream()
                .map(this::convertNutritionPlanToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NutritionPlanDTO> getAllNutritionPlansDTO() {
        logger.info("Извличане на всички хранителни планове.");
        List<NutritionPlan> allPlans = nutritionPlanRepository.findAll();
        return allPlans.stream()
                .map(this::convertNutritionPlanToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<NutritionPlanHistoryDTO> getNutritionPlanHistory(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        List<NutritionPlan> plans = nutritionPlanRepository.findByUserOrderByDateGeneratedDesc(user);

        return plans.stream()
                .map(this::convertNutritionPlanToHistoryDTO)
                .collect(Collectors.toList());
    }


    private NutritionPlanHistoryDTO convertNutritionPlanToHistoryDTO(NutritionPlan plan) {
        if (plan == null) return null;

        initializePlanLazyFields(plan);

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

    @Transactional(readOnly = true)
    private NutritionPlanDTO convertNutritionPlanToDTO(NutritionPlan plan){
        if(plan==null) return null;
        initializePlanLazyFields(plan);
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

    private MealDTO convertMealToMealDTO(com.fitnessapp.model.Meal meal){
        if(meal==null) return null;

        RecipeDTO recipeDTO = convertRecipeToRecipeDTO(meal.getRecipe());

        Double portionSize = Optional.ofNullable(meal.getPortionSize()).orElse(1.0);
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

    private RecipeDTO convertRecipeToRecipeDTO(Recipe recipe){
        if(recipe==null) return null;
        Hibernate.initialize(recipe.getDietType());
        if(recipe.getAllergens() != null) Hibernate.initialize(recipe.getAllergens());
        if(recipe.getTags() != null) Hibernate.initialize(recipe.getTags());

        return RecipeDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .description(recipe.getDescription())
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
                .meatType(recipe.getMeatType())
                .allergens(recipe.getAllergens()!=null? recipe.getAllergens() : Collections.emptySet())
                .tags(recipe.getTags()!=null? recipe.getTags() : Collections.emptySet())
                .build();
    }

    private TrainingPlanDTO convertTrainingPlanToDTO(TrainingPlan plan){
        if(plan==null) return null;
        Hibernate.initialize(plan.getTrainingSessions());
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

    private ExerciseDTO convertExerciseToExerciseDTO(Exercise ex){
        if(ex==null) return null;
        return ExerciseDTO.builder()
                .id(ex.getId())
                .name(ex.getName())
                .description(ex.getDescription())
                .sets(ex.getSets())
                .reps(ex.getReps())
                .durationMinutes(ex.getDurationMinutes())
                .type(ex.getType())
                .difficultyLevel(ex.getDifficultyLevel())
                .equipment(ex.getEquipment())
                .build();
    }

    @Transactional(readOnly = true)
    public FullPlanDTO getFullPlanByUserId(Integer userId){
        User user = userRepository.findById(userId).orElse(null);
        if(user==null){
            logger.warn("User #{} not found", userId);
            return null;
        }
        NutritionPlan nutrition = nutritionPlanRepository.findByUser(user).stream().max(Comparator.comparing(NutritionPlan::getDateGenerated)).orElse(null);
        TrainingPlan  training  = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user).stream().findFirst().orElse(null);

        if (nutrition != null) {
            initializePlanLazyFields(nutrition);
        }
        if (training != null) {
            Hibernate.initialize(training.getTrainingSessions());
            if (training.getTrainingSessions() != null) {
                training.getTrainingSessions().forEach(session -> Hibernate.initialize(session.getExercises()));
            }
        }

        if(nutrition==null && training==null) return FullPlanDTO.builder().build();

        return FullPlanDTO.builder()
                .nutritionPlanId(nutrition!=null? nutrition.getId():null)
                .targetCalories(nutrition!=null? nutrition.getTargetCalories():null)
                .protein(nutrition!=null? nutrition.getProtein():null)
                .fat(nutrition!=null? nutrition.getFat():null)
                .carbohydrates(nutrition!=null? nutrition.getCarbohydrates():null)
                .goalName(nutrition!=null && nutrition.getGoal()!=null? nutrition.getGoal().getName():null)
                .meals(nutrition!=null? nutrition.getMeals().stream().map(this::convertMealToMealDTO).collect(Collectors.toList()): Collections.emptyList())
                .trainingPlanId(training!=null? training.getId():null)
                .trainingPlanDescription(getTrainingSummary(user))
                .trainingDaysPerWeek(training!=null? training.getDaysPerWeek():null)
                .trainingDurationMinutes(training!=null? training.getDurationMinutes():null)
                .trainingSessions(training!=null? training.getTrainingSessions().stream().map(this::convertTrainingSessionToTrainingSessionDTO).collect(Collectors.toList()): Collections.emptyList())
                .build();
    }


    private String getTrainingSummary(User user) {

        TrainingPlan latestTrainingPlan = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user)
                .stream().findFirst().orElse(null);

        if (latestTrainingPlan != null) {
            return String.format("Последен тренировъчен план: %d дни/седмица, %d минути/сесия.",
                    latestTrainingPlan.getDaysPerWeek(), latestTrainingPlan.getDurationMinutes());
        }
        return "Няма наличен тренировъчен план.";
    }

    private void initializePlanLazyFields(NutritionPlan plan){
        if (plan == null) return;
        Hibernate.initialize(plan.getMeals());
        if(plan.getMeals()!=null){
            plan.getMeals().forEach(m->{
                if(m.getRecipe()!=null){
                    Hibernate.initialize(m.getRecipe());
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
            Hibernate.initialize(plan.getWeeklyNutritionPlan().getDailyPlans());
        }
        Hibernate.initialize(plan.getUser());
    }


    private void validateUserProfileForPlanGeneration(User user){
        List<String> missing = new ArrayList<>();
        if(user.getGender()==null)               missing.add("пол");
        if(user.getAge()==null)                  missing.add("възраст");
        if(user.getHeight()==null)               missing.add("ръст");
        if(user.getWeight()==null)               missing.add("тегло");
        if(user.getActivityLevel()==null)        missing.add("ниво на активност");
        if(user.getGoal()==null)                 missing.add("цел");
        if(user.getDietType()==null)             missing.add("диетичен тип");
        if(user.getMeatPreference()==null)       missing.add("предпочитания за месо");
        if(user.getConsumesDairy()==null)        missing.add("консумира млечни продукти");
        if(user.getMealFrequencyPreference()==null) missing.add("честота на хранене");

        if(!missing.isEmpty())
            throw new IllegalArgumentException("Липсват следните данни за генериране на хранителен план: "+String.join(", ", missing));
    }


    private List<Recipe> filterRecipes(List<Recipe> allRecipes, User user) {
        return allRecipes.stream().filter(recipe -> {
            // 1. **ПЪРВО: Приоритизирано филтриране за "БЕЗ МЕСО" / ВЕГЕТАРИАНЕЦ / ВЕГАН**
            // Определяме дали потребителят е вегетарианец/веган въз основа на неговите предпочитания
            boolean userRequiresVegetarianOrVegan = false;
            // Проверка по предпочитание за месо (по-силно)
            if (user.getMeatPreference() == MeatPreferenceType.VEGETARIAN || user.getMeatPreference() == MeatPreferenceType.VEGAN) {
                userRequiresVegetarianOrVegan = true;
            }
            // Проверка по диетичен тип (ако е вегетарианска/веган диета)
            else if (user.getDietType() != null) {
                String userDietTypeName = user.getDietType().getName();
                if ("Вегетарианска".equalsIgnoreCase(userDietTypeName) || "Веган".equalsIgnoreCase(userDietTypeName)) {
                    userRequiresVegetarianOrVegan = true;
                }
            }

            if (userRequiresVegetarianOrVegan) {
                // Ако потребителят е вегетарианец/веган, рецептата ТРЯБВА да е вегетарианска
                if (!Optional.ofNullable(recipe.getIsVegetarian()).orElse(false)) {
                    return false; // Филтрира, ако рецептата НЕ е вегетарианска
                }
                // Ако потребителят е веган, рецептата ТРЯБВА да е веган

                if (user.getMeatPreference() == MeatPreferenceType.VEGAN || ("Веган".equalsIgnoreCase(user.getDietType() != null ? user.getDietType().getName() : null))) {
                    if (!Optional.ofNullable(recipe.getIsVegan()).orElse(false)) {
                        return false; // Филтрира, ако рецептата НЕ е веган
                    }
                }
                // Проверяваме MeatType на рецептата - ако е VEGETARIAN/VEGAN, трябва да е NONE (без месо)
                if (recipe.getMeatType() != null && recipe.getMeatType() != MeatPreferenceType.NONE) {
                    return false; // Филтрира, ако рецептата съдържа някакъв тип месо (различен от NONE)
                }
            }

            // 2. Следващо: Филтриране по КОНКРЕТНИ ИЗКЛЮЧЕНИЯ за месо (ако потребителят не е цялостно без месо)
            // Тази логика е за потребители, които ядат месо, но имат специфични ограничения (напр. без свинско, без риба)
            if (!userRequiresVegetarianOrVegan && user.getMeatPreference() != null) {
                if (user.getMeatPreference() == MeatPreferenceType.NO_PORK && Optional.ofNullable(recipe.getContainsPork()).orElse(false)) {
                    return false; // Филтрира, ако не яде свинско и рецептата съдържа такова
                }
                if (user.getMeatPreference() == MeatPreferenceType.NO_FISH && Optional.ofNullable(recipe.getContainsFish()).orElse(false)) {
                    return false; // Филтрира, ако не яде риба и рецептата съдържа такава
                }

            }


            // 3. Филтриране по ОСНОВЕН ДИЕТИЧЕН ТИП (ако не е 'Балансирана' или 'Без месо' вече се филтрира горе)
            // Ако user.getDietType() е "Балансирана", тогава рецептите с DietType.BALANCED, VEGETARIAN, VEGAN и т.н. са приемливи.
            // Тук се филтрират само рецепти, чийто DietType е различен и НЕ СЪВМЕСТИМ с избрания от потребителя.
            if (user.getDietType() != null && recipe.getDietType() != null) {
                String userDietTypeName = user.getDietType().getName();
                String recipeDietTypeName = recipe.getDietType().getName();

                // Ако потребителят е избрал специфична диета (Кето, Средиземноморска и т.н.), рецептата трябва да отговаря
                if (!"Балансирана".equalsIgnoreCase(userDietTypeName) && // Ако потребителят е избрал БАЛАНСИРАНА, не ограничаваме силно тук
                        !userDietTypeName.equalsIgnoreCase(recipeDietTypeName)) {
                    return false;
                }

            }


            if (user.getAllergies() != null && !user.getAllergies().isEmpty() && recipe.getAllergens() != null) {
                if (user.getAllergies().stream().anyMatch(userAllergy ->
                        recipe.getAllergens().stream().anyMatch(recipeAllergen ->
                                recipeAllergen.equalsIgnoreCase(userAllergy)))) {
                    return false;
                }
            }


            if (user.getConsumesDairy() != null && !user.getConsumesDairy() && Optional.ofNullable(recipe.getContainsDairy()).orElse(false)) {
                return false;
            }

            // Ако всички филтри са преминати, рецептата е подходяща
            return true;
        }).collect(Collectors.toList());
    }

    private void addMeal(List<com.fitnessapp.model.Meal> meals, List<Recipe> recipes, MealType mealType, double targetCaloriesForMeal, Set<Integer> usedRecipeIdsInWeek, Set<Integer> usedRecipeIdsInDay) {
        if (recipes == null || recipes.isEmpty()) {
            logger.warn("Няма налични рецепти за {}. Пропускане.", mealType);
            return;
        }

        double calorieTolerance = targetCaloriesForMeal * 0.15;

        List<Recipe> suitableRecipes = new ArrayList<>();
        for (Recipe recipe : recipes) {
            double recipeCaloriesPerPortion = Optional.ofNullable(recipe.getCalories()).orElse(0.0);
            if (recipeCaloriesPerPortion > 0) {
                double difference = Math.abs(recipeCaloriesPerPortion - targetCaloriesForMeal);
                if (difference <= calorieTolerance) {
                    suitableRecipes.add(recipe);
                }
            }
        }

        Recipe chosenRecipe = null;
        List<Recipe> availableForSelection = new ArrayList<>(suitableRecipes);
        Collections.shuffle(availableForSelection); // Разбъркваме, за да получим случаен ред

        // Опитваме да изберем рецепта, която не е използвана през седмицата И през деня
        for (Recipe recipe : availableForSelection) {
            if (!usedRecipeIdsInWeek.contains(recipe.getId()) && !usedRecipeIdsInDay.contains(recipe.getId())) {
                chosenRecipe = recipe;
                break;
            }
        }

        if (chosenRecipe == null) {
            // Ако всички подходящи рецепти са изчерпани, опитваме да изберем такава, която не е използвана САМО през деня
            // (т.е. може да е била използвана предишни дни от седмицата, но да не се повтаря в същия ден)
            logger.warn("Всички уникални подходящи рецепти за {} са изчерпани за текущата седмица. Опитваме да изберем такава, която не е използвана в текущия ден.", mealType);
            Collections.shuffle(suitableRecipes); // Разбъркваме отново оригиналния suitableRecipes
            for (Recipe recipe : suitableRecipes) {
                if (!usedRecipeIdsInDay.contains(recipe.getId())) {
                    chosenRecipe = recipe;
                    break;
                }
            }
        }

        if (chosenRecipe == null) {
            // Ако и това не помогне, търсим най-близката по калории от всички налични, дори ако е повторение
            logger.warn("Всички уникални подходящи рецепти за {} са изчерпани дори за текущия ден. Избиране на най-близката налична, възможно е повторение.", mealType);
            double minCalorieDifference = Double.MAX_VALUE;
            for (Recipe recipe : suitableRecipes) {
                double recipeCaloriesPerPortion = Optional.ofNullable(recipe.getCalories()).orElse(0.0);
                if (recipeCaloriesPerPortion > 0) {
                    double difference = Math.abs(recipeCaloriesPerPortion - targetCaloriesForMeal);
                    if (difference < minCalorieDifference) {
                        minCalorieDifference = difference;
                        chosenRecipe = recipe;
                    }
                }
            }
            // Краен случай: ако дори и най-близката рецепта не е намерена
            if (chosenRecipe == null && !recipes.isEmpty()){
                Collections.shuffle(recipes); // Избираме напълно произволна от всички
                chosenRecipe = recipes.get(0);
                logger.warn("Всички рецепти за {} са с 0 калории или невалидни. Избрана напълно произволна: '{}'", mealType, chosenRecipe.getName());
            }
        }


        if (chosenRecipe != null) {
            double recipeCaloriesPerPortion = Optional.ofNullable(chosenRecipe.getCalories()).orElse(0.0);
            double portionSize = 1.0;

            if (recipeCaloriesPerPortion > 0) {
                portionSize = targetCaloriesForMeal / recipeCaloriesPerPortion;
            }

            com.fitnessapp.model.Meal meal = com.fitnessapp.model.Meal.builder()
                    .mealType(mealType)
                    .recipe(chosenRecipe)
                    .portionSize(portionSize)
                    .build();
            meals.add(meal);
            usedRecipeIdsInWeek.add(chosenRecipe.getId());
            usedRecipeIdsInDay.add(chosenRecipe.getId());

            logger.info("Добавено хранене: {} (Рецепта: '{}') с порция {:.2f} за целеви калории {:.2f}",
                    mealType, chosenRecipe.getName(), portionSize, targetCaloriesForMeal);
        } else {
            logger.warn("Не беше намерена подходяща рецепта за {}. Моля, проверете наличните рецепти.", mealType);
        }
    }


    @Transactional
    public WeeklyNutritionPlanDTO getOrCreateWeeklyPlan(User user) {
        Optional<WeeklyNutritionPlan> latestPlan = weeklyNutritionPlanRepository.findTopByUserOrderByStartDateDesc(user);
        if (latestPlan.isPresent()) {
            WeeklyNutritionPlan plan = latestPlan.get();
            LocalDate today = LocalDate.now();
            if (!today.isBefore(plan.getStartDate()) && !today.isAfter(plan.getEndDate())) {
                initializeWeeklyPlanLazyFields(plan);
                return convertToWeeklyNutritionPlanDTO(plan);
            }
        }
        return generateAndSaveWeeklyNutritionPlanForUserDTO(user);
    }

    @Transactional
    public WeeklyNutritionPlanDTO replaceMeal(
            Integer planId,
            Integer originalMealId,
            Integer substituteRecipeId) {

        WeeklyNutritionPlan weeklyPlan = weeklyNutritionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Седмичен план с ID " + planId + " не е намерен."));

        initializeWeeklyPlanLazyFields(weeklyPlan);

        com.fitnessapp.model.Meal mealToReplace = weeklyPlan.getDailyPlans().stream()
                .flatMap(dp -> dp.getMeals().stream())
                .filter(m -> m.getId().equals(originalMealId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Хранене с ID " + originalMealId + " не е намерено в седмичния план."));

        Recipe newRecipe = recipeRepository.findById(substituteRecipeId)
                .orElseThrow(() -> new RuntimeException("Рецепта с ID " + substituteRecipeId + " не съществува."));

        mealToReplace.setRecipe(newRecipe);

        NutritionPlan parentDailyPlan = mealToReplace.getNutritionPlan();
        if (parentDailyPlan != null) {
            recalculateNutritionPlanMacros(parentDailyPlan);
            nutritionPlanRepository.save(parentDailyPlan);
        }


        weeklyNutritionPlanRepository.save(weeklyPlan);

        return convertToWeeklyNutritionPlanDTO(weeklyPlan);
    }

    private void recalculateNutritionPlanMacros(NutritionPlan plan) {
        double protein = 0, fat = 0, carbs = 0;
        double actualCalories = 0;

        Hibernate.initialize(plan.getMeals());

        for (com.fitnessapp.model.Meal m : plan.getMeals()) {
            Recipe r = m.getRecipe();
            if (r != null) {
                double currentProtein = Optional.ofNullable(r.getProtein()).orElse(0.0);
                double currentFat = Optional.ofNullable(r.getFat()).orElse(0.0);
                double currentCarbs = Optional.ofNullable(r.getCarbs()).orElse(0.0);
                double currentCalories = Optional.ofNullable(r.getCalories()).orElse(0.0);

                protein += currentProtein * m.getPortionSize();
                fat += currentFat * m.getPortionSize();
                carbs += currentCarbs * m.getPortionSize();
                actualCalories += currentCalories * m.getPortionSize();
            }
        }
        plan.setProtein(protein);
        plan.setFat(fat);
        plan.setCarbohydrates(carbs);
    }

    public WeeklyNutritionPlanDTO convertToWeeklyNutritionPlanDTO(WeeklyNutritionPlan plan) {
        if (plan == null) return null;

        initializeWeeklyPlanLazyFields(plan);

        Map<DayOfWeek, NutritionPlanDTO> dailyDTOs = plan.getDailyPlans().stream()
                .sorted(Comparator.comparing(NutritionPlan::getDateGenerated))
                .collect(Collectors.toMap(
                        NutritionPlan::getDayOfWeek,
                        this::convertNutritionPlanToDTO,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        double totalCal = dailyDTOs.values().stream().mapToDouble(NutritionPlanDTO::getTargetCalories).sum();
        double totalProt = dailyDTOs.values().stream().mapToDouble(NutritionPlanDTO::getProtein).sum();
        double totalCarb = dailyDTOs.values().stream().mapToDouble(NutritionPlanDTO::getCarbohydrates).sum();
        double totalFat = dailyDTOs.values().stream().mapToDouble(NutritionPlanDTO::getFat).sum();

        return WeeklyNutritionPlanDTO.builder()
                .id(plan.getId())
                .userId(plan.getUser().getId())
                .userEmail(plan.getUser().getEmail())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .dailyPlans(dailyDTOs)
                .totalTargetCalories(totalCal)
                .totalProteinGrams(totalProt)
                .totalCarbsGrams(totalCarb)
                .totalFatGrams(totalFat)
                .build();
    }

    private void initializeWeeklyPlanLazyFields(WeeklyNutritionPlan plan) {
        if (plan == null) return;
        Hibernate.initialize(plan.getUser());
        Hibernate.initialize(plan.getDailyPlans());
        if (plan.getDailyPlans() != null) {
            plan.getDailyPlans().forEach(dailyPlan -> {
                initializePlanLazyFields(dailyPlan);
            });
        }
    }
}