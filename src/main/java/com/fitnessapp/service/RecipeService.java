
package com.fitnessapp.service;

import com.fitnessapp.model.Recipe;
import com.fitnessapp.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public Recipe saveRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public List<Recipe> getRecipesByType(String type) {
        return recipeRepository.findByType(type);
    }

    public Recipe getRecipeById(Integer id) {
        return recipeRepository.findById(id).orElse(null);
    }

    public void deleteRecipe(Integer id) {
        recipeRepository.deleteById(id);
    }

    public Recipe updateRecipe(Integer id, Recipe updatedRecipe) {
        Recipe existing = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Рецептата не е намерена"));

        existing.setName(updatedRecipe.getName());
        existing.setDescription(updatedRecipe.getDescription());
        existing.setType(updatedRecipe.getType());
        existing.setCalories(updatedRecipe.getCalories());
        existing.setProtein(updatedRecipe.getProtein());
        existing.setCarbs(updatedRecipe.getCarbs());
        existing.setFat(updatedRecipe.getFat());

        return recipeRepository.save(existing);
    }
}