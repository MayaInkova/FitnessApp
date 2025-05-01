package com.fitnessapp.service;

import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.Recipe;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.NutritionPlanRepository;
import com.fitnessapp.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NutritionPlanService {

    private final NutritionPlanRepository nutritionPlanRepository;
    private final RecipeRepository recipeRepository;

    @Autowired
    public NutritionPlanService(NutritionPlanRepository nutritionPlanRepository,
                                RecipeRepository recipeRepository) {
        this.nutritionPlanRepository = nutritionPlanRepository;
        this.recipeRepository = recipeRepository;
    }

    public NutritionPlan generatePlanForUser(User user) {
        double tdee = NutritionCalculator.calculateTDEE(user);

        double calories = switch (user.getGoal()) {
            case "weight_loss" -> tdee - 500;
            case "muscle_gain" -> tdee + 500;
            default -> tdee;
        };

        // Извличаме рецепти според целта
        List<Recipe> recipes = recipeRepository.findByType(user.getGoal());

        NutritionPlan plan = NutritionPlan.builder()
                .user(user)
                .calories(calories)
                .protein((calories * 0.3) / 4)
                .fat((calories * 0.25) / 9)
                .carbs((calories * 0.45) / 4)
                .goal(user.getGoal())
                .recipes(recipes)
                .build();

        return nutritionPlanRepository.save(plan);
    }

    public NutritionPlan savePlan(NutritionPlan plan) {
        return nutritionPlanRepository.save(plan);
    }

    public NutritionPlan getPlanByUserId(Long userId) {
        return nutritionPlanRepository.findByUserId(userId);
    }

    public List<NutritionPlan> getAllPlans() {
        return nutritionPlanRepository.findAll();
    }
}