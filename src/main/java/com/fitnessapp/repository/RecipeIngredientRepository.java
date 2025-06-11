package com.fitnessapp.repository;

import com.fitnessapp.model.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Integer> {
    List<RecipeIngredient> findByRecipe_Id(Integer recipeId);
    List<RecipeIngredient> findByFoodItem_Id(Integer foodItemId);
}