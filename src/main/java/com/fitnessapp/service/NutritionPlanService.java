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

    // Използва се при регистрация или админ панел
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
                    .build();
            mealRepository.save(meal);
        }

        return savedPlan;
    }

    // Използва се само от Chatbot – не записва в DB
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
}