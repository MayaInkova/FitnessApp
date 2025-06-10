package com.fitnessapp.service;

import com.fitnessapp.dto.FullPlanDTO;
import com.fitnessapp.model.*;
import com.fitnessapp.repository.MealRepository;
import com.fitnessapp.repository.NutritionPlanRepository;
import com.fitnessapp.repository.RecipeRepository;
// import com.fitnessapp.service.NutritionCalculator; // Уверете се, че този импорт е правилен, ако е външен клас
// import com.fitnessapp.service.TrainingPlanService; // Уверете се, че този импорт е правилен, но този Service не трябва да има инжекция на TrainingPlanService, защото може да доведе до циклична зависимост ако TrainingPlanService инжектира NutritionPlanService.
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NutritionPlanService {

    private final NutritionPlanRepository nutritionPlanRepository;
    private final RecipeRepository recipeRepository;
    private final MealRepository mealRepository;
    private final TrainingPlanService trainingPlanService; // NutritionPlanService *генерира* TrainingPlan, така че има нужда от него.

    @Autowired
    public NutritionPlanService(NutritionPlanRepository nutritionPlanRepository,
                                RecipeRepository recipeRepository,
                                MealRepository mealRepository,
                                TrainingPlanService trainingPlanService) {
        this.nutritionPlanRepository = nutritionPlanRepository;
        this.recipeRepository = recipeRepository;
        this.mealRepository = mealRepository;
        this.trainingPlanService = trainingPlanService;
    }

    @Transactional
    public NutritionPlan generatePlanForUser(User user, String dietType) {
        NutritionPlan plan = new NutritionPlan();
        plan.setUser(user);
        plan.setGoal(user.getGoal());

        // Уверете се, че NutritionCalculator е достъпен и методът calculateTDEE е статичен
        double calories = NutritionCalculator.calculateTDEE(user);
        double protein, fat, carbs;

        double calorieAdjustment = 1.0;
        switch (user.getGoal().toLowerCase()) {
            case "weight_loss":
                calorieAdjustment = 0.8;
                break;
            case "muscle_gain":
                calorieAdjustment = 1.15;
                break;
            case "maintain":
            default:
                calorieAdjustment = 1.0;
                break;
        }
        calories *= calorieAdjustment;

        protein = calories * 0.30 / 4;
        fat = calories * 0.25 / 9;
        carbs = (calories - (protein * 4 + fat * 9)) / 4;

        plan.setCalories(Math.round(calories * 100.0) / 100.0);
        plan.setProtein(Math.round(protein * 100.0) / 100.0);
        plan.setFat(Math.round(fat * 100.0) / 100.0);
        plan.setCarbs(Math.round(carbs * 100.0) / 100.0);

        plan.setMeals(new ArrayList<>());
        plan.setRecipes(new ArrayList<>());

        List<Recipe> allRecipes = recipeRepository.findAll();
        List<Recipe> filteredRecipes = filterRecipes(allRecipes, user, dietType);

        if (filteredRecipes.isEmpty()) {
            throw new IllegalStateException("Няма подходящи рецепти за зададените предпочитания и диетичен тип. Моля, добавете още рецепти или променете предпочитанията си.");
        }

        Map<String, List<Recipe>> recipesByType = filteredRecipes.stream()
                .collect(Collectors.groupingBy(r -> r.getType().toLowerCase()));

        Random random = new Random();
        Set<Integer> usedRecipeIdsThisWeek = new HashSet<>();

        List<String> days = List.of("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday");
        List<String> mealTypes = List.of("breakfast", "snack", "lunch", "dinner");

        for (String day : days) {
            Set<Integer> usedRecipeIdsToday = new HashSet<>();
            for (String type : mealTypes) {
                List<Recipe> candidates = recipesByType.getOrDefault(type, Collections.emptyList());

                Recipe selectedRecipe = null;
                List<Recipe> highlyAvailableCandidates = candidates.stream()
                        .filter(r -> !usedRecipeIdsThisWeek.contains(r.getId()) && !usedRecipeIdsToday.contains(r.getId()))
                        .collect(Collectors.toList());

                if (!highlyAvailableCandidates.isEmpty()) {
                    selectedRecipe = highlyAvailableCandidates.get(random.nextInt(highlyAvailableCandidates.size()));
                } else {
                    List<Recipe> dailyAvailableCandidates = candidates.stream()
                            .filter(r -> !usedRecipeIdsToday.contains(r.getId()))
                            .collect(Collectors.toList());

                    if (!dailyAvailableCandidates.isEmpty()) {
                        selectedRecipe = dailyAvailableCandidates.get(random.nextInt(dailyAvailableCandidates.size()));
                    } else {
                        if (!candidates.isEmpty()) {
                            selectedRecipe = candidates.get(random.nextInt(candidates.size()));
                        } else {
                            List<Recipe> fallbackCandidates = filteredRecipes.stream()
                                    .filter(r -> !usedRecipeIdsToday.contains(r.getId()))
                                    .collect(Collectors.toList());
                            if (!fallbackCandidates.isEmpty()) {
                                selectedRecipe = fallbackCandidates.get(random.nextInt(fallbackCandidates.size()));
                            } else {
                                throw new IllegalStateException("Недостатъчно разнообразие от рецепти, моля добавете още или променете предпочитанията си.");
                            }
                        }
                    }
                }

                usedRecipeIdsThisWeek.add(selectedRecipe.getId());
                usedRecipeIdsToday.add(selectedRecipe.getId());

                Meal meal = Meal.builder()
                        .dayOfWeek(day)
                        .type(type)
                        .time(getDefaultTimeForType(type))
                        .recipe(selectedRecipe)
                        .nutritionPlan(plan)
                        .build();

                plan.addMeal(meal);
                if (!plan.getRecipes().contains(selectedRecipe)) {
                    plan.addRecipe(selectedRecipe);
                }
            }
        }

        TrainingPlan training = trainingPlanService.generateTrainingPlanForUser(user);
        plan.setTrainingPlan(training);

        return nutritionPlanRepository.save(plan);
    }

    private List<Recipe> filterRecipes(List<Recipe> recipes, User user, String dietType) {
        final Set<String> userAllergens = user.getAllergies() != null && !user.getAllergies().isBlank() && !user.getAllergies().equalsIgnoreCase("не")
                ? Arrays.stream(user.getAllergies().toLowerCase().split("[,;\\s]+"))
                .map(String::trim)
                .map(s -> "allergy_" + s)
                .collect(Collectors.toSet())
                : Collections.emptySet();

        final String userMeatPreferenceTag = (user.getMeatPreference() != null && !user.getMeatPreference().isBlank())
                ? "meat_" + user.getMeatPreference().toLowerCase().trim()
                : null;

        final String userDietTypeTag = (dietType != null && !dietType.isBlank())
                ? dietType.toLowerCase().trim()
                : null;

        final boolean consumesDairy = user.getConsumesDairy() != null && user.getConsumesDairy();

        return recipes.stream()
                .filter(r -> {
                    Set<String> recipeTags = r.getTags() != null ? r.getTags() : Collections.emptySet();

                    if (userDietTypeTag != null && !userDietTypeTag.isEmpty()) {
                        if (!recipeTags.contains(userDietTypeTag)) {
                            return false;
                        }
                    }

                    boolean hasAllergyConflict = userAllergens.stream().anyMatch(recipeTags::contains);
                    if (hasAllergyConflict) {
                        return false;
                    }

                    boolean recipeHasMeat = recipeTags.stream().anyMatch(tag -> tag.startsWith("meat_"));

                    if (("vegan".equals(userDietTypeTag) || "vegetarian".equals(userDietTypeTag))) {
                        if (recipeHasMeat) {
                            return false;
                        }
                    } else {
                        if (userMeatPreferenceTag != null && !userMeatPreferenceTag.isEmpty()) {
                            if (recipeHasMeat && !recipeTags.contains(userMeatPreferenceTag)) {
                                return false;
                            }
                        }
                        if (userMeatPreferenceTag != null && userMeatPreferenceTag.equalsIgnoreCase("meat_none") && recipeHasMeat) {
                            return false;
                        }
                    }

                    if (!consumesDairy) {
                        if (recipeTags.contains("dairy")) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    private String getDefaultTimeForType(String type) {
        return switch (type.toLowerCase()) {
            case "breakfast" -> "08:00";
            case "snack" -> "10:30";
            case "lunch" -> "13:00";
            case "dinner" -> "19:30";
            default -> "08:00";
        };
    }

    public NutritionPlan savePlan(NutritionPlan plan) {
        return nutritionPlanRepository.save(plan);
    }

    @Transactional
    public NutritionPlan getPlanByUserId(Integer userId) {
        NutritionPlan plan = nutritionPlanRepository.findTopByUserIdOrderByIdDesc(userId);
        if (plan != null) {
            plan.getMeals().size();
            plan.getRecipes().size();
            if (plan.getTrainingPlan() != null) {
                plan.getTrainingPlan().getName();
                plan.getTrainingPlan().getExercises().size();
            }
        }
        return plan;
    }

    @Transactional
    public List<NutritionPlan> getAllNutritionPlansForUser(Integer userId) {
        List<NutritionPlan> plans = nutritionPlanRepository.findAllByUserIdOrderByIdDesc(userId);

        for (NutritionPlan plan : plans) {
            if (plan.getMeals() != null) {
                plan.getMeals().size();
            }
            if (plan.getRecipes() != null) {
                plan.getRecipes().size();
            }
            if (plan.getTrainingPlan() != null) {
                plan.getTrainingPlan().getName();
                if (plan.getTrainingPlan().getExercises() != null) {
                    plan.getTrainingPlan().getExercises().size();
                }
            }
        }
        return plans;
    }

    @Transactional
    public FullPlanDTO getFullPlanByUserId(Integer userId) {
        NutritionPlan plan = getPlanByUserId(userId);
        if (plan == null) return null;

        TrainingPlan trainingPlan = plan.getTrainingPlan();

        List<Exercise> sortedExercises = Collections.emptyList();
        if (trainingPlan != null && trainingPlan.getExercises() != null) {
            List<String> dayOrder = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
            sortedExercises = trainingPlan.getExercises().stream()
                    .sorted(Comparator.comparingInt(e -> dayOrder.indexOf(e.getDayOfWeek())))
                    .collect(Collectors.toList());
        }

        return FullPlanDTO.builder()
                .calories(plan.getCalories())
                .protein(plan.getProtein())
                .fat(plan.getFat())
                .carbs(plan.getCarbs())
                .goal(plan.getGoal())
                .meals(plan.getMeals())
                .recipes(plan.getRecipes())
                .trainingName(trainingPlan != null ? trainingPlan.getName() : null)
                .trainingDescription(trainingPlan != null ? trainingPlan.getDescription() : null)
                .daysPerWeek(trainingPlan != null ? trainingPlan.getDaysPerWeek() : 0)
                .durationMinutes(trainingPlan != null ? trainingPlan.getDurationMinutes() : 0)
                .exercises(sortedExercises)
                .build();
    }

    public FullPlanDTO getFullPlanForUser(Long userId) {
        return getFullPlanByUserId(userId.intValue());
    }

    public List<NutritionPlan> getAllPlans() {
        return nutritionPlanRepository.findAll();
    }

}