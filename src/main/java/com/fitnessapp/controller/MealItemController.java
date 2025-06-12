package com.fitnessapp.controller;

import com.fitnessapp.model.MealItem;
import com.fitnessapp.service.MealItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meal-items")
@CrossOrigin(origins = "*")


public class MealItemController {

    private final MealItemService mealItemService;

    @Autowired
    public MealItemController(MealItemService mealItemService) {
        this.mealItemService = mealItemService;
    }

    @PostMapping
    public ResponseEntity<MealItem> create(@RequestBody MealItem item) {
        return ResponseEntity.ok(mealItemService.saveMealItem(item));
    }

    @GetMapping
    public ResponseEntity<List<MealItem>> getAll() {
        return ResponseEntity.ok(mealItemService.getAllMealItems());
    }

    @GetMapping("/meal/{mealId}")
    public ResponseEntity<List<MealItem>> getByMeal(@PathVariable Integer mealId) {
        return ResponseEntity.ok(mealItemService.getMealItemsByMealId(mealId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        mealItemService.deleteMealItem(id);
        return ResponseEntity.ok("Meal item deleted.");
    }
}