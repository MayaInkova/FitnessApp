package com.fitnessapp.controller;

import com.fitnessapp.model.Recipe;
import com.fitnessapp.model.MealType;
import com.fitnessapp.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "*")
public class RecipeController {

    private static final Logger logger = LoggerFactory.getLogger(RecipeController.class);

    private final RecipeService recipeService;

    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public ResponseEntity<List<Recipe>> getAllRecipes() {
        List<Recipe> recipes = recipeService.getAllRecipes();
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable Integer id) {
        Optional<Recipe> recipe = recipeService.getRecipeById(id);
        return recipe.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Recipe with ID {} not found.", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // Променяме името на ендпойнта и начина на работа с MealType
    @GetMapping("/by-meal-type/{mealTypeString}") // Използваме String от пътя
    public ResponseEntity<List<Recipe>> getRecipesByMealType(@PathVariable String mealTypeString) {
        try {
            // Преобразуваме String в MealType enum
            MealType mealType = MealType.fromString(mealTypeString); // Извикваме fromString
            List<Recipe> recipes = recipeService.getRecipesByMealType(mealType);
            return ResponseEntity.ok(recipes);
        } catch (IllegalArgumentException e) {
            logger.error("Невалиден тип хранене '{}' при търсене на рецепти: {}", mealTypeString, e.getMessage());
            return ResponseEntity.badRequest().body(null); // Връщаме 400 Bad Request
        } catch (Exception e) {
            logger.error("Грешка при търсене на рецепти по тип хранене '{}': {}", mealTypeString, e.getMessage(), e);
            return ResponseEntity.status(500).body(null); // Връщаме 500 Internal Server Error
        }
    }


    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')") // Пример за сигурност
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        try {
            Recipe savedRecipe = recipeService.saveRecipe(recipe);
            return ResponseEntity.ok(savedRecipe);
        } catch (Exception e) {
            logger.error("Грешка при създаване на рецепта: ", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // Пример за сигурност
    public ResponseEntity<Recipe> updateRecipe(@PathVariable Integer id, @RequestBody Recipe recipe) {
        try {
            Recipe updatedRecipe = recipeService.updateRecipe(id, recipe);
            return ResponseEntity.ok(updatedRecipe);
        } catch (RuntimeException e) { // Хващаме RuntimeException, хвърлен от service, ако рецептата не е намерена
            logger.warn("Рецепта с ID {} не е намерена за актуализация: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Грешка при актуализация на рецепта с ID {}: ", id, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // Пример за сигурност
    public ResponseEntity<Void> deleteRecipe(@PathVariable Integer id) {
        try {
            recipeService.deleteRecipe(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (Exception e) {
            logger.error("Грешка при изтриване на рецепта с ID {}: ", id, e);
            return ResponseEntity.status(500).build();
        }
    }
}