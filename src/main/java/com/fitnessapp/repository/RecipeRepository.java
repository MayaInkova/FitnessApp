package com.fitnessapp.repository;

import com.fitnessapp.model.MealType;
import com.fitnessapp.model.Recipe;
import com.fitnessapp.model.DietType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Integer> {


    List<Recipe> findByMealType(MealType mealType);
    List<Recipe> findByDietType(DietType dietType);
    List<Recipe> findByMealTypeAndIdNot(MealType mealType, Integer id);
    boolean existsByName(String name);


    @Query("""
           SELECT COUNT(r) 
           FROM Recipe r
           WHERE r.mealType = :mealType
             AND r.id <> :recipeId
           """)
    long countByMealTypeAndIdNot(@Param("mealType") MealType mealType,
                                 @Param("recipeId") Integer recipeId);
}