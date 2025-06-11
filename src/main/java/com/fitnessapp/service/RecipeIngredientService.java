package com.fitnessapp.service;

import com.fitnessapp.model.RecipeIngredient;
import com.fitnessapp.repository.RecipeIngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeIngredientService {

    private final RecipeIngredientRepository recipeIngredientRepository;

    @Autowired
    public RecipeIngredientService(RecipeIngredientRepository recipeIngredientRepository) {
        this.recipeIngredientRepository = recipeIngredientRepository;
    }

    public RecipeIngredient saveRecipeIngredient(RecipeIngredient item) {
        return recipeIngredientRepository.save(item);
    }

    public List<RecipeIngredient> getByRecipeId(Integer recipeId) {
        // КОРИГИРАНО: Използвай findByRecipe_Id, както е дефинирано в Repository
        return recipeIngredientRepository.findByRecipe_Id(recipeId);
    }

    public List<RecipeIngredient> getAll() {
        return recipeIngredientRepository.findAll();
    }

    public void delete(Integer id) {
        recipeIngredientRepository.deleteById(id);
    }
}