package com.fitnessapp.service;

import com.fitnessapp.dto.FullPlanDTO;
import com.fitnessapp.model.*;
import com.fitnessapp.repository.MealRepository;
import com.fitnessapp.repository.NutritionPlanRepository;
import com.fitnessapp.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NutritionPlanService {

    private final NutritionPlanRepository nutritionPlanRepository;
    private final RecipeRepository recipeRepository;
    private final MealRepository mealRepository;
    private final TrainingPlanService trainingPlanService;

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

    public NutritionPlan generatePlanForUser(User user, String dietType) {
        double tdee = NutritionCalculator.calculateTDEE(user);

        double calories = switch (user.getGoal()) {
            case "weight_loss" -> tdee - 500;
            case "muscle_gain" -> tdee + 500;
            default -> tdee;
        };

        double proteinRatio = 0.3, fatRatio = 0.25, carbsRatio = 0.45;

        if (dietType != null) {
            switch (dietType.toLowerCase()) {
                case "keto" -> {
                    proteinRatio = 0.25;
                    fatRatio = 0.65;
                    carbsRatio = 0.10;
                }
                case "high_protein" -> {
                    proteinRatio = 0.40;
                    fatRatio = 0.30;
                    carbsRatio = 0.30;
                }
            }
        }

        double protein = calories * proteinRatio / 4;
        double fat = calories * fatRatio / 9;
        double carbs = calories * carbsRatio / 4;

        NutritionPlan plan = NutritionPlan.builder()
                .user(user)
                .calories(calories)
                .protein(protein)
                .fat(fat)
                .carbs(carbs)
                .goal(user.getGoal())
                .build();

        NutritionPlan savedPlan = nutritionPlanRepository.save(plan);

        List<Recipe> allRecipes = recipeRepository.findAll();
        List<Recipe> filtered = filterRecipes(allRecipes, user);
        if (filtered.isEmpty()) filtered = allRecipes;
        if (filtered.isEmpty()) throw new RuntimeException("Няма налични рецепти в базата.");

        List<Recipe> selectedRecipes = new ArrayList<>(filtered.stream().limit(5).toList());
        for (Recipe recipe : selectedRecipes) {
            Meal meal = Meal.builder()
                    .recipe(recipe)
                    .type(recipe.getType())
                    .time(getSuggestedTimeForMeal(recipe.getType()))
                    .build();
            savedPlan.addMeal(meal);
        }

        savedPlan.setRecipes(selectedRecipes);

        boolean prefersWeights = user.getTrainingType() != null && user.getTrainingType().toLowerCase().contains("тежест");
        TrainingPlan trainingPlan = trainingPlanService.getRecommended(user.getGoal(), prefersWeights);
        savedPlan.setTrainingPlan(trainingPlan);

        return nutritionPlanRepository.save(savedPlan);
    }

    private List<Recipe> filterRecipes(List<Recipe> recipes, User user) {
        final List<String> allergens = (user.getAllergies() != null && !user.getAllergies().isBlank() && !user.getAllergies().equalsIgnoreCase("не"))
                ? Arrays.asList(user.getAllergies().toLowerCase().split("[,;\\s]+"))
                : Collections.emptyList();

        return recipes.stream()
                .filter(r -> {
                    String ingredients = Optional.ofNullable(r.getIngredients()).orElse("").toLowerCase();
                    boolean matchesMeat = user.getMeatPreference() == null || ingredients.contains(user.getMeatPreference().toLowerCase());
                    boolean matchesDairy = user.getConsumesDairy() == null || user.getConsumesDairy() ||
                            (!ingredients.contains("мляко") && !ingredients.contains("сирене") &&
                                    !ingredients.contains("кисело мляко") && !ingredients.contains("масло"));
                    boolean matchesAllergies = allergens.stream().noneMatch(ingredients::contains);
                    return matchesMeat && matchesDairy && matchesAllergies;
                })
                .toList();
    }

    private String getSuggestedTimeForMeal(String type) {
        if (type == null) return "08:00";
        return switch (type.toLowerCase()) {
            case "breakfast", "закуска" -> "08:00";
            case "lunch", "обяд" -> "13:00";
            case "snack", "междинно" -> "16:00";
            case "dinner", "вечеря" -> "19:00";
            default -> "10:30";
        };
    }

    @Transactional
    public FullPlanDTO getFullPlanByUserId(Integer userId) {
        NutritionPlan plan = nutritionPlanRepository.findTopByUserIdOrderByIdDesc(userId);
        if (plan == null) {
            System.out.println(" Не е намерен хранителен план за userId: " + userId);
            return null;
        }

        TrainingPlan trainingPlan = plan.getTrainingPlan();
        System.out.println(" Зареден trainingPlan: " + (trainingPlan != null ? trainingPlan.getName() : "null"));

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
                .exercises(trainingPlan != null && trainingPlan.getExercises() != null ? trainingPlan.getExercises() : Collections.emptyList())
                .build();
    }

    public FullPlanDTO getFullPlanForUser(Long userId) {
        return getFullPlanByUserId(userId.intValue());
    }

    public NutritionPlan savePlan(NutritionPlan plan) {
        return nutritionPlanRepository.save(plan);
    }

    public List<NutritionPlan> generateWeeklyPlanForUser(User user, String dietType) {
        List<NutritionPlan> plans = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            plans.add(generatePlanForUser(user, dietType));
        }
        return plans;
    }

    public List<NutritionPlan> getAllPlans() {
        return nutritionPlanRepository.findAll();
    }

    public List<NutritionPlan> getAllByUserId(Integer userId) {
        return nutritionPlanRepository.findAllByUserIdOrderByIdDesc(userId);
    }

    public NutritionPlan getPlanByUserId(Integer userId) {
        return nutritionPlanRepository.findTopByUserIdOrderByIdDesc(userId);
    }
}