package com.fitnessapp.repository;

import com.fitnessapp.model.NutritionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionPlanRepository extends JpaRepository<NutritionPlan, Integer> {
    NutritionPlan findByUserId(Integer userId);
}