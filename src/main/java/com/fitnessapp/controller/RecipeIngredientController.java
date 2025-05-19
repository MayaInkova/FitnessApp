package com.fitnessapp.controller;

import com.fitnessapp.model.RecipeIngredient;
import com.fitnessapp.service.RecipeIngredientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipe-ingredients")
@CrossOrigin(origins = "*")
public class RecipeIngredientController {

    private final RecipeIngredientService recipeIngredientService;

    @Autowired
    public RecipeIngredientController(RecipeIngredientService recipeIngredientService) {
        this.recipeIngredientService = recipeIngredientService;
    }

    @PostMapping
    public ResponseEntity<RecipeIngredient> create(@RequestBody RecipeIngredient item) {
        return ResponseEntity.ok(recipeIngredientService.saveRecipeIngredient(item));
    }

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<List<RecipeIngredient>> getByRecipe(@PathVariable Integer recipeId) {
        return ResponseEntity.ok(recipeIngredientService.getByRecipeId(recipeId));
    }

    @GetMapping
    public ResponseEntity<List<RecipeIngredient>> getAll() {
        return ResponseEntity.ok(recipeIngredientService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        recipeIngredientService.delete(id);
        return ResponseEntity.ok("Recipe ingredient deleted.");
    }
}