package com.fitnessapp.repository;

import com.fitnessapp.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    List<Recipe> findByType(String type); // За филтриране по тип (закуска, обяд и т.н.)
    List<Recipe> findTop5ByTypeIn(List<String> types);
    List<Recipe> findTop5ByCaloriesLessThanOrderByCaloriesAsc(Double maxCalories);
    List<Recipe> findTop5ByProteinGreaterThanOrderByProteinDesc(Double minProtein);
    List<Recipe> findTop5ByOrderByCaloriesAsc(); // fallback вариант
}
