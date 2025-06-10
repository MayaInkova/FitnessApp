package com.fitnessapp.repository;

import com.fitnessapp.model.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // <-- Увери се, че имаш този import
import java.util.Optional;

@Repository
public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Integer> {



    List<TrainingPlan> findByGoalAndWithWeights(String goal, boolean withWeights);


    List<TrainingPlan> findByGoal(String goal);
    List<TrainingPlan> findByWithWeights(boolean withWeights);
}