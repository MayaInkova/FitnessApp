package com.fitnessapp.repository;

import com.fitnessapp.model.NutritionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NutritionPlanRepository extends JpaRepository<NutritionPlan, Integer> {
    NutritionPlan findByUserId(Integer userId);
    List<NutritionPlan> findAllByUserIdOrderByIdDesc(Integer userId); //  за история
}