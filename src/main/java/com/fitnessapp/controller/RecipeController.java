package com.fitnessapp.controller;

import com.fitnessapp.model.Recipe;
import com.fitnessapp.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "*")
public class RecipeController {

    private final RecipeService recipeService;

    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    //  Създаване на рецепта
    @PostMapping
    public ResponseEntity<?> createRecipe(@RequestBody Recipe recipe) {
        Recipe saved = recipeService.saveRecipe(recipe);
        return ResponseEntity.ok(saved);
    }

    //  Вземане на всички рецепти
    @GetMapping
    public ResponseEntity<?> getAllRecipes() {
        return ResponseEntity.ok(recipeService.getAllRecipes());
    }

    //  Вземане по тип (напр. "закуска")
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getRecipesByType(@PathVariable String type) {
        return ResponseEntity.ok(recipeService.getRecipesByType(type));
    }

    //  Вземане по ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getRecipeById(@PathVariable Long id) {
        Recipe recipe = recipeService.getRecipeById(id);
        return recipe != null ? ResponseEntity.ok(recipe) : ResponseEntity.notFound().build();
    }

    //  Изтриване
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.ok("Рецептата е изтрита.");
    }
}