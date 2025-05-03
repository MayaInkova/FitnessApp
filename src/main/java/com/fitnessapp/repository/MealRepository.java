package com.fitnessapp.repository;

import com.fitnessapp.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;



public interface MealRepository extends JpaRepository<Meal, Long> {
    @Query("SELECT m FROM Meal m WHERE m.plan.id = :planId")
    List<Meal> findByPlanId(@Param("planId") Long planId);
}
