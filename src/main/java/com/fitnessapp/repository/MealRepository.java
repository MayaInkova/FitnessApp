package com.fitnessapp.repository;

import com.fitnessapp.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;




public interface MealRepository extends JpaRepository<Meal, Integer> {

    @Query("SELECT m FROM Meal m WHERE m.nutritionPlan.id = :planId")
    List<Meal> findByNutritionPlanId(@Param("planId") Integer planId);
}