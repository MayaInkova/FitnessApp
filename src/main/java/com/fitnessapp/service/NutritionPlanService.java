package com.fitnessapp.service;

import com.fitnessapp.dto.FullPlanDTO;
import com.fitnessapp.dto.MealDTO;
import com.fitnessapp.dto.RecipeDTO;
import com.fitnessapp.dto.TrainingSessionDTO;
import com.fitnessapp.dto.ExerciseDTO;

import com.fitnessapp.model.*;
import com.fitnessapp.repository.NutritionPlanRepository;
import com.fitnessapp.repository.RecipeRepository;
import com.fitnessapp.repository.TrainingPlanRepository;
import com.fitnessapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
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


        if (chosenBreakfast != null)
            nutritionPlan.addMeal(Meal.builder().recipe(chosenBreakfast).mealType(MealType.BREAKFAST).portionSize(1.0).build());
        if (chosenLunch != null)
            nutritionPlan.addMeal(Meal.builder().recipe(chosenLunch).mealType(MealType.LUNCH).portionSize(1.0).build());
        if (chosenDinner != null)
            nutritionPlan.addMeal(Meal.builder().recipe(chosenDinner).mealType(MealType.DINNER).portionSize(1.0).build());

        if (user.getMealFrequencyPreference() != null && user.getMealFrequencyPreference().name().equals("THREE_TIMES_DAILY")) { // Changed to match MealFrequencyPreferenceType enum
            if (chosenSnack1 != null)
                nutritionPlan.addMeal(Meal.builder().recipe(chosenSnack1).mealType(MealType.SNACK).portionSize(1.0).build());
            if (chosenSnack2 != null)
                nutritionPlan.addMeal(Meal.builder().recipe(chosenSnack2).mealType(MealType.SNACK).portionSize(1.0).build());
        }

        return nutritionPlanRepository.save(nutritionPlan);
    }

    private List<Recipe> filterRecipes(List<Recipe> recipes, User user) {
        String meatPref = Optional.ofNullable(user.getMeatPreference())
                .map(Enum::name)
                .orElse("")
                .toLowerCase();

        Set<String> allergies = Optional.ofNullable(user.getAllergies())
                .orElse(Collections.emptySet())
                .stream().map(String::toLowerCase).collect(Collectors.toSet());

        String dietType = user.getDietType() != null ? user.getDietType().getName().toLowerCase() : "";
        Boolean consumesDairy = user.getConsumesDairy();

        return recipes.stream().filter(recipe -> {

            if (recipe.getDietType() != null && !dietType.isEmpty() && !recipe.getDietType().getName().equalsIgnoreCase(dietType)) {
                return false;
            }

            if (recipe.getAllergens() != null && recipe.getAllergens().stream().anyMatch(allergies::contains)) {
                return false;
            }


            if (("none".equals(meatPref) || "без месо".equalsIgnoreCase(meatPref)) && recipe.getMeatType() != null && !"none".equalsIgnoreCase(recipe.getMeatType().name())) {
                return false; // If the user is "no meat", exclude recipes with a meat type other than "none"
            }
            if (("vegetarian".equals(meatPref) || "вегетарианска".equalsIgnoreCase(meatPref)) && Boolean.FALSE.equals(recipe.getIsVegetarian())) {
                return false; // If the user is vegetarian, exclude non-vegetarian recipes
            }

            if (!meatPref.isEmpty() && !"none".equals(meatPref) && !"vegetarian".equals(meatPref) && recipe.getMeatType() != null && !meatPref.equalsIgnoreCase(recipe.getMeatType().name())) {
                return false;
            }


            if (Boolean.FALSE.equals(consumesDairy) && Boolean.TRUE.equals(recipe.getContainsDairy())) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    public List<NutritionPlan> getNutritionPlansByUser(User user) {
        return nutritionPlanRepository.findByUser(user);
    }

    public String getTrainingSummary(User user) {
        Optional<TrainingPlan> trainingPlanOpt = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user).stream().findFirst();

        if (trainingPlanOpt.isPresent()) {
            TrainingPlan plan = trainingPlanOpt.get();
            StringBuilder sb = new StringBuilder("Тренировъчен план за ")
                    .append(plan.getDateGenerated()).append(":\n")
                    .append("Дни: ").append(plan.getDaysPerWeek()).append("\n")
                    .append("Продължителност: ").append(plan.getDurationMinutes()).append(" мин\n\n");

            for (TrainingSession session : Optional.ofNullable(plan.getTrainingSessions()).orElse(Collections.emptyList())) {
                sb.append("  ").append(session.getDayOfWeek()).append(" (")
                        .append(session.getDurationMinutes()).append(" мин.):\n");
                for (Exercise exercise : Optional.ofNullable(session.getExercises()).orElse(Collections.emptyList())) {
                    sb.append("    - ").append(exercise.getName()).append(" (").append(exercise.getDescription()).append(")\n");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
        return "Няма намерен тренировъчен план.";
    }

    @Transactional
    public NutritionPlan saveNutritionPlan(NutritionPlan plan) {

        if (plan.getUser() != null && plan.getUser().getId() != null) {
            User managedUser = userRepository.findById(plan.getUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException("User with ID " + plan.getUser().getId() + " not found."));
            plan.setUser(managedUser);
        } else if (plan.getUser() != null) { // If the user is new, but without an ID
            userRepository.save(plan.getUser()); // Save the user first
        }
        return nutritionPlanRepository.save(plan);
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
                    .meals(mealDTOs) // КОРЕКЦИЯ: Използваме MealDTOs
                    .trainingPlanId(trainingPlan != null ? trainingPlan.getId() : null)
                    .trainingPlanDescription(getTrainingSummary(user))
                    .trainingDaysPerWeek(trainingPlan != null ? trainingPlan.getDaysPerWeek() : null)
                    .trainingDurationMinutes(trainingPlan != null ? trainingPlan.getDurationMinutes() : null)
                    .trainingSessions(trainingSessionDTOs) // КОРЕКЦИЯ: Използваме TrainingSessionDTOs
                    .build();

        } catch (Exception e) {
            logger.error("Error retrieving full plan for user ID: {}", userId, e);
            return null;
        }
    }

    // --- Помощни методи за преобразуване от Entity към DTO ---

    private RecipeDTO convertRecipeToRecipeDTO(Recipe recipe) {
        if (recipe == null) {
            return null;
        }
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

    private MealDTO convertMealToMealDTO(Meal meal) {
        if (meal == null) {
            return null;
        }
        RecipeDTO recipeDTO = convertRecipeToRecipeDTO(meal.getRecipe());

        return MealDTO.builder()
                .id(meal.getId())
                .recipe(recipeDTO)
                .mealType(meal.getMealType())
                .portionSize(meal.getPortionSize())
                .build();
    }

    private ExerciseDTO convertExerciseToExerciseDTO(Exercise exercise) {
        if (exercise == null) {
            return null;
        }
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
        if (session == null) {
            return null;
        }
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


