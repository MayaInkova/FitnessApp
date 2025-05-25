package com.fitnessapp.controller;



import com.fitnessapp.dto.NutritionPlanDTO;
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
import java.util.stream.Collectors;

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

    @PostMapping
    public ResponseEntity<?> createNutritionPlan(@RequestBody NutritionPlan plan) {
        try {
            var recipes = plan.getRecipes();
            plan.setRecipes(null);

            NutritionPlan savedPlan = nutritionPlanService.savePlan(plan);
            savedPlan.setRecipes(recipes);
            NutritionPlan updatedPlan = nutritionPlanService.savePlan(savedPlan);

            return ResponseEntity.ok(updatedPlan);
        } catch (Exception e) {
            logger.error("Грешка при създаване на хранителен план: ", e);
            return ResponseEntity.status(500).body("Грешка при създаване на план: " + e.getMessage());
        }
    }

    @PostMapping("/generate/{userId}")
    public ResponseEntity<?> generatePlan(@PathVariable Integer userId) {
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

    @GetMapping("/{userId}")
    public ResponseEntity<?> getNutritionPlanByUserId(@PathVariable Integer userId) {
        try {
            NutritionPlan plan = nutritionPlanService.getPlanByUserId(userId);
            return plan != null ? ResponseEntity.ok(plan) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Грешка при вземане на план по потребител: ", e);
            return ResponseEntity.status(500).body("Възникна грешка: " + e.getMessage());
        }
    }

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


    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getPlanHistory(@PathVariable Integer userId) {
        try {
            List<NutritionPlan> plans = nutritionPlanService.getAllByUserId(userId);

            List<NutritionPlanDTO> dtoList = plans.stream()
                    .map(p -> new NutritionPlanDTO(
                            p.getId(),
                            p.getCalories(),
                            p.getProtein(),
                            p.getFat(),
                            p.getCarbs(),
                            p.getGoal()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);
        } catch (Exception e) {
            logger.error("Грешка при зареждане на историята на плановете: ", e);
            return ResponseEntity.status(500).body("Грешка при зареждане на историята");
        }
    }
}