package com.fitnessapp.controller;


import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.service.NutritionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nutrition-plans")
@CrossOrigin(origins = "*")

public class NutritionPlanController {

    private final NutritionPlanService nutritionPlanService;

    @Autowired
    public NutritionPlanController(NutritionPlanService nutritionPlanService) {
        this.nutritionPlanService = nutritionPlanService;
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
}