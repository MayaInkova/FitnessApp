package com.fitnessapp.controller;

import com.fitnessapp.model.FoodItem;
import com.fitnessapp.service.FoodItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food-items")
@CrossOrigin(origins = "*")
public class FoodItemController {

    private final FoodItemService foodItemService;

    @Autowired
    public FoodItemController(FoodItemService foodItemService) {
        this.foodItemService = foodItemService;
    }

    @PostMapping
    public ResponseEntity<FoodItem> create(@RequestBody FoodItem item) {
        return ResponseEntity.ok(foodItemService.saveFoodItem(item));
    }

    @GetMapping
    public ResponseEntity<List<FoodItem>> getAll() {
        return ResponseEntity.ok(foodItemService.getAllFoodItems());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        foodItemService.deleteFoodItem(id);
        return ResponseEntity.ok("Food item deleted.");
    }
}