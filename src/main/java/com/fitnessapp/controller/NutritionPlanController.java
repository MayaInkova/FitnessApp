package com.fitnessapp.controller;


import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.User;
import com.fitnessapp.service.NutritionPlanService;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fitnessapp.service.UserService;


@Builder
@RestController
@RequestMapping("/api/nutrition-plans")
@CrossOrigin(origins = "*")

public class NutritionPlanController {


    private final NutritionPlanService nutritionPlanService;
    private final UserService userService;


    @Autowired
    public NutritionPlanController(NutritionPlanService nutritionPlanService, UserService userService) {
        this.nutritionPlanService = nutritionPlanService;
        this.userService = userService;

    }

    @PostMapping
    public ResponseEntity<?> createNutritionPlan(@RequestBody NutritionPlan plan) {
        NutritionPlan savedPlan = nutritionPlanService.savePlan(plan);
        return ResponseEntity.ok(savedPlan);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getNutritionPlanByUserId(@PathVariable Long userId) {
        NutritionPlan plan = nutritionPlanService.getPlanByUserId(userId);
        return plan != null ? ResponseEntity.ok(plan) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<?> getAllNutritionPlans() {
        return ResponseEntity.ok(nutritionPlanService.getAllPlans());
    }

    @PostMapping("/generate/{userId}")
    public ResponseEntity<?> generatePlan(@PathVariable Long userId) {
        User user = userService.getUserById(userId); // използваме инжектирания обект
        if (user == null) return ResponseEntity.notFound().build();

        NutritionPlan plan = nutritionPlanService.generatePlanForUser(user);
        return ResponseEntity.ok(plan);
    }
}