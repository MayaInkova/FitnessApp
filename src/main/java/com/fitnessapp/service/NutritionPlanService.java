package com.fitnessapp.service;

import com.fitnessapp.model.Meal;
import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.Recipe;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.MealRepository;
import com.fitnessapp.repository.NutritionPlanRepository;
import com.fitnessapp.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NutritionPlanService {

    private final NutritionPlanRepository nutritionPlanRepository;
    private final RecipeRepository recipeRepository;
    private final MealRepository mealRepository;

    @Autowired
    public NutritionPlanService(NutritionPlanRepository nutritionPlanRepository,
                                RecipeRepository recipeRepository,
                                MealRepository mealRepository) {
        this.nutritionPlanRepository = nutritionPlanRepository;
        this.recipeRepository = recipeRepository;
        this.mealRepository = mealRepository;
    }

    public NutritionPlan generatePlanForUser(User user) {
        double tdee = NutritionCalculator.calculateTDEE(user);

        double calories = switch (user.getGoal()) {
            case "weight_loss" -> tdee - 500;
            case "muscle_gain" -> tdee + 500;
            default -> tdee;
        };

        NutritionPlan plan = NutritionPlan.builder()
                .user(user)
                .calories(calories)
                .protein((calories * 0.3) / 4)
                .fat((calories * 0.25) / 9)
                .carbs((calories * 0.45) / 4)
                .goal(user.getGoal())
                .build();

        NutritionPlan savedPlan = nutritionPlanRepository.save(plan);

        List<Recipe> recipes = switch (user.getGoal()) {
            case "weight_loss" -> recipeRepository.findTop5ByCaloriesLessThanOrderByCaloriesAsc(400.0);
            case "muscle_gain" -> recipeRepository.findTop5ByProteinGreaterThanOrderByProteinDesc(25.0);
            default -> recipeRepository.findTop5ByOrderByCaloriesAsc();
        };

        savedPlan.setRecipes(recipes);
        savedPlan = nutritionPlanRepository.save(savedPlan);

        for (Recipe recipe : recipes) {
            Meal meal = Meal.builder()
                    .nutritionPlan(savedPlan)
                    .recipe(recipe)
                    .type(recipe.getType())
                    .time(getSuggestedTimeForMeal(recipe.getType()))
                    .build();
            mealRepository.save(meal);
        }

        List<Meal> meals = mealRepository.findByNutritionPlanId(savedPlan.getId());
        savedPlan.setMeals(meals);

        return savedPlan;
    }

    public List<NutritionPlan> generateWeeklyPlanForUser(User user) {
        List<NutritionPlan> plans = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            NutritionPlan dailyPlan = generatePlanForUser(user);
            plans.add(dailyPlan);
        }
        return plans;
    }

    public NutritionPlan calculatePlanForUser(User user) {
        double tdee = NutritionCalculator.calculateTDEE(user);

        double calories = switch (user.getGoal()) {
            case "weight_loss" -> tdee - 500;
            case "muscle_gain" -> tdee + 500;
            default -> tdee;
        };

        List<Recipe> recipes = switch (user.getGoal()) {
            case "weight_loss" -> recipeRepository.findTop5ByCaloriesLessThanOrderByCaloriesAsc(400.0);
            case "muscle_gain" -> recipeRepository.findTop5ByProteinGreaterThanOrderByProteinDesc(25.0);
            default -> recipeRepository.findTop5ByOrderByCaloriesAsc();
        };

        return NutritionPlan.builder()
                .calories(calories)
                .protein((calories * 0.3) / 4)
                .fat((calories * 0.25) / 9)
                .carbs((calories * 0.45) / 4)
                .goal(user.getGoal())
                .recipes(recipes)
                .build();
    }

    public NutritionPlan savePlan(NutritionPlan plan) {
        return nutritionPlanRepository.save(plan);
    }

    public NutritionPlan getPlanByUserId(Integer userId) {
        return nutritionPlanRepository.findByUserId(userId);
    }

    public List<NutritionPlan> getAllPlans() {
        List<NutritionPlan> plans = nutritionPlanRepository.findAll();
        if (plans == null || plans.isEmpty()) {
            throw new RuntimeException("Няма налични планове.");
        }
        return plans;
    }

    public List<NutritionPlan> getAllByUserId(Integer userId) {
        return nutritionPlanRepository.findAllByUserIdOrderByIdDesc(userId);
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
}
