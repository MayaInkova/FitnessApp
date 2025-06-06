package com.fitnessapp.repository;

import com.fitnessapp.model.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Integer> {
    Optional<TrainingPlan> findFirstByGoalAndWithWeights(String goal, boolean withWeights);
}
