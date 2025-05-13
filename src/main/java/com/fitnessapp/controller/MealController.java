package com.fitnessapp.controller;


import com.fitnessapp.model.Meal;
import com.fitnessapp.service.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meals")
@CrossOrigin(origins = "*")

public class MealController {

    private final MealService mealService;

    @Autowired
    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    //  Създаване на хранене
    @PostMapping
    public ResponseEntity<?> createMeal(@RequestBody Meal meal) {
        Meal savedMeal = mealService.saveMeal(meal);
        return ResponseEntity.ok(savedMeal);
    }

    //  Връщане на всички хранения
    @GetMapping
    public ResponseEntity<List<Meal>> getAllMeals() {
        return ResponseEntity.ok(mealService.getAllMeals());
    }

    //  Връщане на хранения по план ID
    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<Meal>> getMealsByPlanId(@PathVariable Integer planId) {
        return ResponseEntity.ok(mealService.getMealsByPlanId(planId));
    }

    //  Изтриване на хранене
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMeal(@PathVariable Integer id) {
        mealService.deleteMeal(id);
        return ResponseEntity.ok("Храненето е изтрито.");
    }
}