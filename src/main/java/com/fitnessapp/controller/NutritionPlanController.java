package com.fitnessapp.controller;


import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.User;
import com.fitnessapp.service.NutritionPlanService;
import com.fitnessapp.service.UserService;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Builder
@RestController
@RequestMapping("/api/nutrition-plans")
@CrossOrigin(origins = "*")


public class NutritionPlanController {

    private final NutritionPlanService nutritionPlanService;
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(NutritionPlanController.class);

    @Autowired
    public NutritionPlanController(NutritionPlanService nutritionPlanService, UserService userService) {
        this.nutritionPlanService = nutritionPlanService;
        this.userService = userService;
    }

    // Създаване на план с рецепти (устойчиво на FK грешки)
    @PostMapping
    public ResponseEntity<?> createNutritionPlan(@RequestBody NutritionPlan plan) {
        try {
            var recipes = plan.getRecipes(); // временно премахваме рецептите
            plan.setRecipes(null);

            NutritionPlan savedPlan = nutritionPlanService.savePlan(plan); // запис без рецепти

            savedPlan.setRecipes(recipes); // добавяме обратно рецептите
            NutritionPlan updatedPlan = nutritionPlanService.savePlan(savedPlan);

            return ResponseEntity.ok(updatedPlan);
        } catch (Exception e) {
            logger.error("Грешка при създаване на хранителен план: ", e);
            return ResponseEntity.status(500).body("Грешка при създаване на план: " + e.getMessage());
        }
    }

    //  Генериране на план от бекенда по логика (BMR, TDEE и т.н.)
    @PostMapping("/generate/{userId}")
    public ResponseEntity<?> generatePlan(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) return ResponseEntity.notFound().build();

            NutritionPlan plan = nutritionPlanService.generatePlanForUser(user);
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            logger.error("Грешка при генериране на план: ", e);
            return ResponseEntity.status(500).body("Грешка при генериране: " + e.getMessage());
        }
    }

    // Връщане на план по потребител ID
    @GetMapping("/{userId}")
    public ResponseEntity<?> getNutritionPlanByUserId(@PathVariable Long userId) {
        try {
            NutritionPlan plan = nutritionPlanService.getPlanByUserId(userId);
            return plan != null ? ResponseEntity.ok(plan) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Грешка при вземане на план по потребител: ", e);
            return ResponseEntity.status(500).body("Възникна грешка: " + e.getMessage());
        }
    }

    // Връщане на всички планове (за админ)
    @GetMapping
    public ResponseEntity<?> getAllNutritionPlans() {
        try {
            List<NutritionPlan> plans = nutritionPlanService.getAllPlans();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            logger.error("Грешка при зареждане на всички планове: ", e);
            return ResponseEntity.status(500).body("Грешка при зареждане: " + e.getMessage());
        }
    }
}