package com.fitnessapp.repository;

import com.fitnessapp.model.MealType;
import com.fitnessapp.model.Recipe;
import com.fitnessapp.model.DietType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Integer> {

    List<Recipe> findByMealType(MealType mealType);

    List<Recipe> findByDietType(DietType dietType);
}
