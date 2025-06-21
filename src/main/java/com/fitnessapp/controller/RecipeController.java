package com.fitnessapp.controller;

import com.fitnessapp.dto.RecipeDTO;
import com.fitnessapp.model.MealType;
import com.fitnessapp.model.Recipe;
import com.fitnessapp.service.RecipeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "*")
public class RecipeController {

    private static final Logger logger = LoggerFactory.getLogger(RecipeController.class);

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /** Всички рецепти като DTO */
    @GetMapping
    public ResponseEntity<List<RecipeDTO>> getAllRecipes() {
        List<RecipeDTO> recipes = recipeService.getAllRecipeDTOs();
        return ResponseEntity.ok(recipes);
    }

    /** Една рецепта по ID */
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> getRecipeById(@PathVariable Integer id) {
        return recipeService.getRecipeDTOById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Recipe with ID {} not found.", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /** Рецепти по MealType като DTO */
    @GetMapping("/by-meal-type/{mealTypeString}")
    public ResponseEntity<List<RecipeDTO>> getRecipesByMealType(@PathVariable String mealTypeString) {
        try {
            MealType mealType = MealType.fromString(mealTypeString);
            return ResponseEntity.ok(recipeService.getRecipeDTOsByMealType(mealType));
        } catch (IllegalArgumentException e) {
            logger.error("Невалиден тип хранене '{}' при търсене на рецепти.", mealTypeString);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Грешка при търсене на рецепти по тип хранене '{}'", mealTypeString, e);
            return ResponseEntity.status(500).build();
        }
    }


    @GetMapping("/alternatives")
    public ResponseEntity<List<RecipeDTO>> getAlternatives(@RequestParam MealType mealType,
                                                           @RequestParam Integer excludeId) {
        List<RecipeDTO> alts = recipeService.findAlternatives(mealType, excludeId);
        return ResponseEntity.ok(alts);
    }


    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        try {
            return ResponseEntity.ok(recipeService.saveRecipe(recipe));
        } catch (Exception e) {
            logger.error("Грешка при създаване на рецепта.", e);
            return ResponseEntity.status(500).build();
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable Integer id, @RequestBody Recipe recipe) {
        try {
            return ResponseEntity.ok(recipeService.updateRecipe(id, recipe));
        } catch (RuntimeException e) {
            logger.warn("Рецепта с ID {} не е намерена за актуализация.", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Грешка при актуализация на рецепта с ID {}", id, e);
            return ResponseEntity.status(500).build();
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Integer id) {
        try {
            recipeService.deleteRecipe(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Грешка при изтриване на рецепта с ID {}", id, e);
            return ResponseEntity.status(500).build();
        }
    }
}
