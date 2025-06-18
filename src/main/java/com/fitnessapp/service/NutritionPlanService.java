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
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class NutritionPlanService {

    private static final Logger log = LoggerFactory.getLogger(NutritionPlanService.class);


    private final NutritionPlanRepository nutritionPlanRepository;
    private final RecipeRepository        recipeRepository;
    private final TrainingPlanRepository  trainingPlanRepository;
    private final UserRepository          userRepository;
    private final TrainingPlanService     trainingPlanService;


    @Transactional
    public NutritionPlanDTO generateAndSaveNutritionPlanForUserDTO(User user){
        log.info("Генериране и запазване на хранителен план за потребител: {}", user.getFullName());
        NutritionPlan entity = generateNutritionPlan(user);
        return convertNutritionPlanToDTO(entity);
    }


    @Transactional
    public NutritionPlan generateNutritionPlan(User user){

        validateUserProfileForPlanGeneration(user);


        Optional<NutritionPlan> existing = nutritionPlanRepository.findByUserAndDateGenerated(user, LocalDate.now());
        if(existing.isPresent()){
            NutritionPlan plan = existing.get();
            initializePlanLazyFields(plan);
            return plan;
        }


        double targetCalories = NutritionCalculator.calculateTDEE(user);
        if(user.getGoal()!=null && user.getGoal().getCalorieModifier()!=null){
            targetCalories += user.getGoal().getCalorieModifier();
        }
        log.info("TDEE за {}: {} kcal", user.getEmail(), targetCalories);


        List<Recipe> filtered = filterRecipes(recipeRepository.findAll(), user);
        if(filtered.isEmpty())
            throw new IllegalStateException("Няма рецепти, отговарящи на критериите.");


        Map<MealType,List<Recipe>> byType = filtered.stream().collect(Collectors.groupingBy(Recipe::getMealType));

        Recipe breakfast = byType.getOrDefault(MealType.BREAKFAST, Collections.emptyList()).stream().findFirst().orElse(null);
        Recipe lunch     = byType.getOrDefault(MealType.LUNCH,     Collections.emptyList()).stream().findFirst().orElse(null);
        Recipe dinner    = byType.getOrDefault(MealType.DINNER,    Collections.emptyList()).stream().findFirst().orElse(null);
        List<Recipe> snacks = byType.getOrDefault(MealType.SNACK,  Collections.emptyList());


        NutritionPlan plan = new NutritionPlan();
        plan.setUser(user);
        plan.setTargetCalories(targetCalories);
        plan.setDateGenerated(LocalDate.now());



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
        plan.setGoal(user.getGoal()); // Запазваме референция към целта на потребителя


        List<com.fitnessapp.model.Meal> meals = new ArrayList<>();
        if(breakfast!=null) meals.add(com.fitnessapp.model.Meal.builder().recipe(breakfast).mealType(MealType.BREAKFAST).portionSize(1.0).build());
        if(lunch!=null)     meals.add(com.fitnessapp.model.Meal.builder().recipe(lunch)    .mealType(MealType.LUNCH)    .portionSize(1.0).build());
        if(dinner!=null)    meals.add(com.fitnessapp.model.Meal.builder().recipe(dinner)   .mealType(MealType.DINNER)   .portionSize(1.0).build());

        if(user.getMealFrequencyPreference()==MealFrequencyPreferenceType.FIVE_TIMES_DAILY){
            if(snacks.size()>0) meals.add(com.fitnessapp.model.Meal.builder().recipe(snacks.get(0)).mealType(MealType.SNACK).portionSize(1.0).build());
            if(snacks.size()>1) meals.add(com.fitnessapp.model.Meal.builder().recipe(snacks.get(1)).mealType(MealType.SNACK).portionSize(1.0).build());
        }
        plan.setMeals(meals);

        // изчисляваме макроси
        double protein = 0, fat = 0, carbs = 0;
        for(com.fitnessapp.model.Meal m: meals){
            Recipe r = m.getRecipe();
            if(r!=null){
                protein += Optional.ofNullable(r.getProtein()).orElse(0.0) * m.getPortionSize();
                fat     += Optional.ofNullable(r.getFat())    .orElse(0.0) * m.getPortionSize();
                carbs   += Optional.ofNullable(r.getCarbs())  .orElse(0.0) * m.getPortionSize();
            }
        }
        plan.setProtein(protein);
        plan.setFat(fat);
        plan.setCarbohydrates(carbs);

        NutritionPlan saved = nutritionPlanRepository.save(plan);
        log.info("NutritionPlan #{} запазен за {}", saved.getId(), user.getEmail());
        return saved;
    }



    @Transactional
    public NutritionPlanDTO saveNutritionPlan(NutritionPlan plan) {
        log.info("Записване на NutritionPlan (директно): {}", plan.getId());
        NutritionPlan savedPlan = nutritionPlanRepository.save(plan);
        return convertNutritionPlanToDTO(savedPlan);
    }


    @Transactional(readOnly = true)
    public List<NutritionPlanDTO> getNutritionPlansByUserDTO(User user) {
        log.info("Извличане на хранителни планове за потребител: {}", user.getEmail());
        List<NutritionPlan> plans = nutritionPlanRepository.findByUser(user);
        // Сортиране по дата на генериране в низходящ ред (най-новият първи)
        plans.sort(Comparator.comparing(NutritionPlan::getDateGenerated).reversed());
        return plans.stream()
                .map(this::convertNutritionPlanToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<NutritionPlanDTO> getAllNutritionPlansDTO() {
        log.info("Извличане на всички хранителни планове.");
        List<NutritionPlan> allPlans = nutritionPlanRepository.findAll();
        return allPlans.stream()
                .map(this::convertNutritionPlanToDTO)
                .collect(Collectors.toList());
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
        return MealDTO.builder()
                .id(meal.getId())
                .mealType(meal.getMealType())
                .portionSize(meal.getPortionSize())
                .recipe(convertRecipeToRecipeDTO(meal.getRecipe()))
                .build();
    }

    private RecipeDTO convertRecipeToRecipeDTO(Recipe recipe){
        if(recipe==null) return null;
        Hibernate.initialize(recipe.getDietType());
        Hibernate.initialize(recipe.getAllergens());
        Hibernate.initialize(recipe.getTags()); // Ensure tags are initialized
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
                // Уверете се, че 'containsPork' е правилно мапнато, ако съществува в Recipe entity
                // .containsPork(recipe.getContainsPork())
                .meatType(recipe.getMeatType()!=null? recipe.getMeatType():null)
                // КОРЕКЦИЯ: Подаваме Set<String> директно, тъй като RecipeDTO очаква Set<String>
                .allergens(recipe.getAllergens()!=null? recipe.getAllergens() : Collections.emptySet())
                .tags(recipe.getTags()!=null? recipe.getTags() : Collections.emptySet())
                .build();
    }

    private TrainingPlanDTO convertTrainingPlanToDTO(TrainingPlan plan){
        if(plan==null) return null;
        Hibernate.initialize(plan.getTrainingSessions());
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
                .build();
    }



    @Transactional(readOnly = true)
    public FullPlanDTO getFullPlanByUserId(Integer userId){
        User user = userRepository.findById(userId).orElse(null);
        if(user==null){
            log.warn("User #{} not found", userId);
            return null;
        }
        NutritionPlan nutrition = nutritionPlanRepository.findByUser(user).stream().max(Comparator.comparing(NutritionPlan::getDateGenerated)).orElse(null);
        TrainingPlan  training  = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user).stream().findFirst().orElse(null);
        if(nutrition==null && training==null) return FullPlanDTO.builder().build();
        return FullPlanDTO.builder()
                .nutritionPlanId(nutrition!=null? nutrition.getId():null)
                .targetCalories(nutrition!=null? nutrition.getTargetCalories():null)
                .protein(nutrition!=null? nutrition.getProtein():null)
                .fat(nutrition!=null? nutrition.getFat():null)
                .carbohydrates(nutrition!=null? nutrition.getCarbohydrates():null)
                .goalName(nutrition!=null && nutrition.getGoal()!=null? nutrition.getGoal().getName():null)
                .meals(nutrition!=null? nutrition.getMeals().stream().map(this::convertMealToMealDTO).collect(Collectors.toList()): null)
                .trainingPlanId(training!=null? training.getId():null)
                .trainingPlanDescription(getTrainingSummary(user))
                .trainingDaysPerWeek(training!=null? training.getDaysPerWeek():null)
                .trainingDurationMinutes(training!=null? training.getDurationMinutes():null)
                .trainingSessions(training!=null? training.getTrainingSessions().stream().map(this::convertTrainingSessionToTrainingSessionDTO).collect(Collectors.toList()): null)
                .build();
    }



    private void initializePlanLazyFields(NutritionPlan plan){
        Hibernate.initialize(plan.getMeals());
        if(plan.getMeals()!=null){
            plan.getMeals().forEach(m->{
                if(m.getRecipe()!=null){
                    Hibernate.initialize(m.getRecipe().getDietType());
                    Hibernate.initialize(m.getRecipe().getAllergens());
                    Hibernate.initialize(m.getRecipe().getTags());
                }
            });
        }
        Hibernate.initialize(plan.getGoal());
        // Инициализираме Lazy заредените snapshot полета, ако са обекти (ManyToOne)
        Hibernate.initialize(plan.getUserActivityLevelSnapshot());
        Hibernate.initialize(plan.getUserDietTypeSnapshot());
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
        if(!missing.isEmpty())
            throw new IllegalArgumentException("Липсват следните данни: "+String.join(", ", missing));
    }

    private List<Recipe> filterRecipes(List<Recipe> all, User user){

        return all;
    }

    public String getTrainingSummary(User user){
        Optional<TrainingPlan> opt = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user).stream().findFirst();
        if(opt.isEmpty()) return "Няма намерен тренировъчен план.";
        TrainingPlan plan = opt.get();
        Hibernate.initialize(plan.getTrainingSessions());
        StringBuilder sb = new StringBuilder();
        sb.append("Тренировъчен план от ").append(plan.getDateGenerated()).append('\n');
        for(TrainingSession sess : plan.getTrainingSessions()){
            sb.append("  ").append(sess.getDayOfWeek()).append(" – ").append(sess.getDurationMinutes()).append(" мин\n");
            Hibernate.initialize(sess.getExercises());
            for(Exercise ex: sess.getExercises()) sb.append("    - ").append(ex.getName()).append('\n');
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public List<NutritionPlanHistoryDTO> getNutritionPlanHistory(Integer userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        List<NutritionPlan> plans = nutritionPlanRepository.findByUserOrderByDateGeneratedDesc(user);

        return plans.stream()
                .map(plan -> {
                    // Инициализирайте Lazy-loaded обекти, преди да ги използвате
                    Hibernate.initialize(plan.getGoal());
                    Hibernate.initialize(plan.getUserActivityLevelSnapshot());
                    Hibernate.initialize(plan.getUserDietTypeSnapshot());
                    // Не е необходимо да инициализирате стринговите snapshot-и, тъй като те са директно в entity

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
                })
                .collect(Collectors.toList());
    }
}