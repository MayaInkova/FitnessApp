package com.fitnessapp.repository;

import com.fitnessapp.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Integer> {
    List<Meal> findByNutritionPlan_Id(Integer nutritionPlanId);
}