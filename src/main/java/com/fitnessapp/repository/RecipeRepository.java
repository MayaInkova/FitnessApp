package com.fitnessapp.repository;

import com.fitnessapp.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByType(String type); // За филтриране по тип (закуска, обяд и т.н.)
}