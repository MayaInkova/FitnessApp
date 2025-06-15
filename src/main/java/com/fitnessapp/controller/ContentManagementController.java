package com.fitnessapp.controller;

import com.fitnessapp.model.Goal;
import com.fitnessapp.model.Recipe;
import com.fitnessapp.service.GoalService;
import com.fitnessapp.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/content")
@CrossOrigin(origins = "*")
public class ContentManagementController {

    @Autowired
    private GoalService goalService;

    @Autowired
    private RecipeService recipeService;



    @GetMapping("/goals")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Goal> getAllGoals() {
        return goalService.getAllGoals();
    }

    @PutMapping("/goals/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateGoal(@PathVariable Integer id, @RequestBody Goal updated) {
        Goal goal = goalService.updateGoal(id, updated);
        return ResponseEntity.ok(goal);
    }

    @DeleteMapping("/goals/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteGoal(@PathVariable Integer id) {
        goalService.deleteGoal(id);
        return ResponseEntity.ok(" Целта е изтрита.");
    }


    @GetMapping("/recipes")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Recipe> getAllRecipes() {
        return recipeService.getAllRecipes();
    }

    @DeleteMapping("/recipes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRecipe(@PathVariable Integer id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.ok(" Рецептата е изтрита.");
    }

    @PutMapping("/recipes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRecipe(@PathVariable Integer id, @RequestBody Recipe updated) {
        Recipe recipe = recipeService.updateRecipe(id, updated);
        return ResponseEntity.ok(recipe);
    }
}