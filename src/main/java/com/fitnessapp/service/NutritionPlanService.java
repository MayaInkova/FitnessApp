package com.fitnessapp.service;

import com.fitnessapp.dto.FullPlanDTO;
import com.fitnessapp.dto.MealDTO;
import com.fitnessapp.dto.RecipeDTO;
import com.fitnessapp.dto.TrainingSessionDTO;
import com.fitnessapp.dto.ExerciseDTO;
import com.fitnessapp.model.ActivityLevel;
import com.fitnessapp.model.DietType;
import com.fitnessapp.model.GenderType;
import com.fitnessapp.model.GoalType;
import com.fitnessapp.model.MealFrequencyPreferenceType;
import com.fitnessapp.model.MealType;
import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.Recipe;
import com.fitnessapp.model.TrainingPlan;
import com.fitnessapp.model.TrainingSession;
import com.fitnessapp.model.Exercise;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.NutritionPlanRepository;
import com.fitnessapp.repository.RecipeRepository;
import com.fitnessapp.repository.TrainingPlanRepository;
import com.fitnessapp.repository.UserRepository;
import com.fitnessapp.service.NutritionCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NutritionPlanService {

    private static final Logger logger = LoggerFactory.getLogger(NutritionPlanService.class);

    private final NutritionPlanRepository nutritionPlanRepository;
    private final RecipeRepository recipeRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final UserRepository userRepository;

    @Autowired
    public NutritionPlanService(NutritionPlanRepository nutritionPlanRepository,
                                RecipeRepository recipeRepository,
                                TrainingPlanRepository trainingPlanRepository,
                                UserRepository userRepository) {
        this.nutritionPlanRepository = nutritionPlanRepository;
        this.recipeRepository = recipeRepository;
        this.trainingPlanRepository = trainingPlanRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NutritionPlan generateNutritionPlan(User user) {
        // ПРОВЕРКА ЗА ЛИПСВАЩИ ПОТРЕБИТЕЛСКИ ДАННИ
        validateUserProfileForPlanGeneration(user);

        double targetCalories = NutritionCalculator.calculateTDEE(user);

        if (user.getGoal() != null && user.getGoal().getCalorieModifier() != null) {
            targetCalories += user.getGoal().getCalorieModifier();
        }

        List<Recipe> allRecipes = recipeRepository.findAll();
        List<Recipe> filteredRecipes = filterRecipes(allRecipes, user);

        List<Recipe> breakfastRecipes = filteredRecipes.stream().filter(r -> r.getMealType() == MealType.BREAKFAST).collect(Collectors.toList());
        List<Recipe> lunchRecipes = filteredRecipes.stream().filter(r -> r.getMealType() == MealType.LUNCH).collect(Collectors.toList());
        List<Recipe> dinnerRecipes = filteredRecipes.stream().filter(r -> r.getMealType() == MealType.DINNER).collect(Collectors.toList());
        List<Recipe> snackRecipes = filteredRecipes.stream().filter(r -> r.getMealType() == MealType.SNACK).collect(Collectors.toList());

        Recipe chosenBreakfast = breakfastRecipes.stream().findFirst().orElse(null);
        Recipe chosenLunch = lunchRecipes.stream().findFirst().orElse(null);
        Recipe chosenDinner = dinnerRecipes.stream().findFirst().orElse(null);
        Recipe chosenSnack1 = snackRecipes.stream().findFirst().orElse(null);
        Recipe chosenSnack2 = snackRecipes.stream().skip(1).findFirst().orElse(null);

        NutritionPlan nutritionPlan = new NutritionPlan();
        nutritionPlan.setUser(user);
        nutritionPlan.setTargetCalories(targetCalories);
        nutritionPlan.setDateGenerated(LocalDate.now());

        List<com.fitnessapp.model.Meal> meals = new ArrayList<>();
        if (chosenBreakfast != null) {
            meals.add(com.fitnessapp.model.Meal.builder().recipe(chosenBreakfast).mealType(MealType.BREAKFAST).portionSize(1.0).build());
        }
        if (chosenLunch != null) {
            meals.add(com.fitnessapp.model.Meal.builder().recipe(chosenLunch).mealType(MealType.LUNCH).portionSize(1.0).build());
        }
        if (chosenDinner != null) {
            meals.add(com.fitnessapp.model.Meal.builder().recipe(chosenDinner).mealType(MealType.DINNER).portionSize(1.0).build());
        }

        if (user.getMealFrequencyPreference() != null && user.getMealFrequencyPreference() == MealFrequencyPreferenceType.FIVE_TIMES_DAILY) {
            if (chosenSnack1 != null) {
                meals.add(com.fitnessapp.model.Meal.builder().recipe(chosenSnack1).mealType(MealType.SNACK).portionSize(1.0).build());
            }
            if (chosenSnack2 != null) {
                meals.add(com.fitnessapp.model.Meal.builder().recipe(chosenSnack2).mealType(MealType.SNACK).portionSize(1.0).build());
            }
        }

        nutritionPlan.setMeals(meals);

        return nutritionPlanRepository.save(nutritionPlan);
    }

    private void validateUserProfileForPlanGeneration(User user) {
        List<String> missingFields = new ArrayList<>();
        if (user.getGender() == null) missingFields.add("пол");
        if (user.getAge() == null) missingFields.add("възраст");
        if (user.getHeight() == null) missingFields.add("ръст");
        if (user.getWeight() == null) missingFields.add("тегло");
        if (user.getActivityLevel() == null) missingFields.add("ниво на активност");
        if (user.getGoal() == null) missingFields.add("цел");
        if (user.getDietType() == null) missingFields.add("диетичен тип");

        if (!missingFields.isEmpty()) {
            String errorMessage = "Моля, попълнете всички задължителни данни за профила си (липсващи: " +
                    String.join(", ", missingFields) +
                    ") чрез чатбота или секция 'Профил', преди да генерирате план.";
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private List<Recipe> filterRecipes(List<Recipe> recipes, User user) {
        String meatPrefName = Optional.ofNullable(user.getMeatPreference())
                .map(Enum::name)
                .orElse("")
                .toLowerCase();

        Set<String> allergies = Optional.ofNullable(user.getAllergies())
                .orElse(Collections.emptySet())
                .stream().map(String::toLowerCase).collect(Collectors.toSet());

        String dietTypeName = user.getDietType() != null ? user.getDietType().getName().toLowerCase() : "";
        Boolean consumesDairy = user.getConsumesDairy();

        return recipes.stream().filter(recipe -> {
            if (recipe.getDietType() != null && !dietTypeName.isEmpty() && !recipe.getDietType().getName().equalsIgnoreCase(dietTypeName)) {
                return false;
            }

            if (recipe.getAllergens() != null && recipe.getAllergens().stream().anyMatch(allergies::contains)) {
                return false;
            }

            if (recipe.getMeatType() != null) {
                if (meatPrefName.equals("none") && !recipe.getMeatType().name().equalsIgnoreCase("none")) {
                    return false;
                }
                if (meatPrefName.equals("vegetarian") && !recipe.getIsVegetarian()) {
                    return false;
                }
                if (!meatPrefName.isEmpty() && !meatPrefName.equals("none") && !meatPrefName.equals("vegetarian")) {
                    if (!meatPrefName.equalsIgnoreCase(recipe.getMeatType().name())) {
                        return false;
                    }
                }
            }

            if (Boolean.FALSE.equals(consumesDairy) && Boolean.TRUE.equals(recipe.getContainsDairy())) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    public NutritionPlan saveNutritionPlan(NutritionPlan plan) {
        if (plan.getUser() != null && plan.getUser().getId() != null) {
            User managedUser = userRepository.findById(plan.getUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException("User with ID " + plan.getUser().getId() + " not found."));
            plan.setUser(managedUser);
        } else if (plan.getUser() != null) {
            userRepository.save(plan.getUser());
        }
        return nutritionPlanRepository.save(plan);
    }

    public List<NutritionPlan> getNutritionPlansByUser(User user) {
        return nutritionPlanRepository.findByUser(user);
    }

    public List<NutritionPlan> getAllNutritionPlans() {
        return nutritionPlanRepository.findAll();
    }

    public FullPlanDTO getFullPlanByUserId(Integer userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.warn("User with ID {} not found for full plan retrieval.", userId);
                return null;
            }

            List<NutritionPlan> nutritionPlans = nutritionPlanRepository.findByUser(user);
            NutritionPlan lastNutritionPlan = nutritionPlans.stream()
                    .max(Comparator.comparing(NutritionPlan::getDateGenerated))
                    .orElse(null);

            TrainingPlan trainingPlan = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user)
                    .stream().findFirst().orElse(null);

            if (lastNutritionPlan == null && trainingPlan == null) {
                logger.info("No nutrition or training plan found for user ID {}.", userId);
                return null;
            }

            List<MealDTO> mealDTOs = null;
            if (lastNutritionPlan != null && lastNutritionPlan.getMeals() != null) {
                mealDTOs = lastNutritionPlan.getMeals().stream()
                        .map(this::convertMealToMealDTO)
                        .collect(Collectors.toList());
            }

            List<TrainingSessionDTO> trainingSessionDTOs = null;
            if (trainingPlan != null && trainingPlan.getTrainingSessions() != null) {
                trainingSessionDTOs = trainingPlan.getTrainingSessions().stream()
                        .map(this::convertTrainingSessionToTrainingSessionDTO)
                        .collect(Collectors.toList());
            }

            return FullPlanDTO.builder()
                    .nutritionPlanId(lastNutritionPlan != null ? lastNutritionPlan.getId() : null)
                    .targetCalories(lastNutritionPlan != null ? lastNutritionPlan.getTargetCalories() : null)
                    .protein(lastNutritionPlan != null ? lastNutritionPlan.getProtein() : null)
                    .fat(lastNutritionPlan != null ? lastNutritionPlan.getFat() : null)
                    .carbohydrates(lastNutritionPlan != null ? lastNutritionPlan.getCarbohydrates() : null)
                    .goalName(lastNutritionPlan != null && lastNutritionPlan.getGoal() != null ? lastNutritionPlan.getGoal().getName() : null)
                    .meals(mealDTOs)
                    .trainingPlanId(trainingPlan != null ? trainingPlan.getId() : null)
                    .trainingPlanDescription(getTrainingSummary(user))
                    .trainingDaysPerWeek(trainingPlan != null ? trainingPlan.getDaysPerWeek() : null)
                    .trainingDurationMinutes(trainingPlan != null ? trainingPlan.getDurationMinutes() : null)
                    .trainingSessions(trainingSessionDTOs)
                    .build();

        } catch (Exception e) {
            logger.error("Error retrieving full plan for user ID: {}", userId, e);
            return null;
        }
    }

    public String getTrainingSummary(User user) {
        Optional<TrainingPlan> trainingPlanOpt = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user).stream().findFirst();

        if (trainingPlanOpt.isPresent()) {
            TrainingPlan plan = trainingPlanOpt.get();
            StringBuilder sb = new StringBuilder("Тренировъчен план за ")
                    .append(plan.getDateGenerated()).append(":\n")
                    .append("Дни: ").append(Optional.ofNullable(plan.getDaysPerWeek()).orElse(0)).append("\n")
                    .append("Продължителност: ").append(Optional.ofNullable(plan.getDurationMinutes()).orElse(0)).append(" мин\n\n");

            for (TrainingSession session : Optional.ofNullable(plan.getTrainingSessions()).orElse(Collections.emptyList())) {
                sb.append("  ").append(Optional.ofNullable(session.getDayOfWeek()).map(Enum::name).orElse("Неизвестен ден")).append(" (")
                        .append(Optional.ofNullable(session.getDurationMinutes()).orElse(0)).append(" мин.):\n");
                for (Exercise exercise : Optional.ofNullable(session.getExercises()).orElse(Collections.emptyList())) {
                    sb.append("    - ").append(Optional.ofNullable(exercise.getName()).orElse("Неизвестно упражнение")).append(" (").append(Optional.ofNullable(exercise.getDescription()).orElse("")).append(")\n");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
        return "Няма намерен тренировъчен план.";
    }

    private RecipeDTO convertRecipeToRecipeDTO(Recipe recipe) {
        if (recipe == null) return null;
        return RecipeDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .description(recipe.getDescription())
                .imageUrl(recipe.getImageUrl())
                .calories(recipe.getCalories())
                .protein(recipe.getProtein())
                .carbs(recipe.getCarbs())
                .fat(recipe.getFat())
                .isVegetarian(recipe.getIsVegetarian())
                .containsDairy(recipe.getContainsDairy())
                .containsNuts(recipe.getContainsNuts())
                .containsFish(recipe.getContainsFish())
                .containsPork(recipe.getContainsPork())
                .tags(recipe.getTags())
                .mealType(recipe.getMealType())
                .instructions(recipe.getInstructions())
                .dietTypeName(recipe.getDietType() != null ? recipe.getDietType().getName() : null)
                .allergens(recipe.getAllergens())
                .meatType(recipe.getMeatType())
                .build();
    }

    private MealDTO convertMealToMealDTO(com.fitnessapp.model.Meal meal) {
        if (meal == null) return null;
        RecipeDTO recipeDTO = convertRecipeToRecipeDTO(meal.getRecipe());
        return MealDTO.builder()
                .id(meal.getId())
                .recipe(recipeDTO)
                .mealType(meal.getMealType())
                .portionSize(meal.getPortionSize())
                .build();
    }

    private ExerciseDTO convertExerciseToExerciseDTO(Exercise exercise) {
        if (exercise == null) return null;
        return ExerciseDTO.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .description(exercise.getDescription())
                .sets(exercise.getSets())
                .reps(exercise.getReps())
                .durationMinutes(exercise.getDurationMinutes())
                .type(exercise.getType())
                .difficultyLevel(exercise.getDifficultyLevel())
                .equipment(exercise.getEquipment())
                .build();
    }

    private TrainingSessionDTO convertTrainingSessionToTrainingSessionDTO(TrainingSession session) {
        if (session == null) return null;
        List<ExerciseDTO> exerciseDTOs = null;
        if (session.getExercises() != null) {
            exerciseDTOs = session.getExercises().stream()
                    .map(this::convertExerciseToExerciseDTO)
                    .collect(Collectors.toList());
        }
        return TrainingSessionDTO.builder()
                .id(session.getId())
                .dayOfWeek(session.getDayOfWeek())
                .durationMinutes(session.getDurationMinutes())
                .exercises(exerciseDTOs)
                .build();
    }

    public void fixMissingTrainingPlans() {
        logger.info("Fixing missing training plans (implementation pending).");

    }
}
