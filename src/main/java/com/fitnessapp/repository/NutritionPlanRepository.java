package com.fitnessapp.repository;


import com.fitnessapp.model.NutritionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NutritionPlanRepository extends JpaRepository<NutritionPlan, Integer> {

    // Връща всички планове на потребител, подредени по ID (от най-нов към най-стар)
    List<NutritionPlan> findAllByUserIdOrderByIdDesc(Integer userId);

    // Връща само най-новия план на потребител
    NutritionPlan findTopByUserIdOrderByIdDesc(Integer userId);
}