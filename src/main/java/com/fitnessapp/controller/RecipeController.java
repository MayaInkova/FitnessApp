package com.fitnessapp.controller;

import com.fitnessapp.model.Recipe;
import com.fitnessapp.model.MealType;
import com.fitnessapp.dto.RecipeDTO;
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
    public ResponseEntity<List<RecipeDTO>> getAllRecipes() {
        List<RecipeDTO> recipes = recipeService.getAllRecipeDTOs();
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> getRecipeById(@PathVariable Integer id) {
        Optional<RecipeDTO> recipe = recipeService.getRecipeDTOById(id);
        return recipe.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Recipe with ID {} not found.", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/by-meal-type/{mealTypeString}")
    public ResponseEntity<List<RecipeDTO>> getRecipesByMealType(@PathVariable String mealTypeString) { // ПРОМЕНЕНО: Връща List<RecipeDTO>
        try {
            MealType mealType = MealType.fromString(mealTypeString);
            List<RecipeDTO> recipes = recipeService.getRecipeDTOsByMealType(mealType); // Извиква новия метод
            return ResponseEntity.ok(recipes);
        } catch (IllegalArgumentException e) {
            logger.error("Невалиден тип хранене '{}' при търсене на рецепти: {}", mealTypeString, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Грешка при търсене на рецепти по тип хранене '{}': {}", mealTypeString, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping
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
    public ResponseEntity<Recipe> updateRecipe(@PathVariable Integer id, @RequestBody Recipe recipe) { // Връща Recipe Entity, ако е необходимо
        try {
            Recipe updatedRecipe = recipeService.updateRecipe(id, recipe);
            return ResponseEntity.ok(updatedRecipe);
        } catch (RuntimeException e) {
            logger.warn("Рецепта с ID {} не е намерена за актуализация: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Грешка при актуализация на рецепта с ID {}: ", id, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deleteRecipe(@PathVariable Integer id) {
        try {
            recipeService.deleteRecipe(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Грешка при изтриване на рецепта с ID {}: ", id, e);
            return ResponseEntity.status(500).build();
        }
    }
}