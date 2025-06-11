package com.fitnessapp.repository;

import com.fitnessapp.model.MealItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealItemRepository extends JpaRepository<MealItem, Integer> {
    List<MealItem> findByMeal_Id(Integer mealId);
    List<MealItem> findByRecipe_Id(Integer recipeId);
}