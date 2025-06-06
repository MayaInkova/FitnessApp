package com.fitnessapp.repository;

import com.fitnessapp.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {
    List<Exercise> findByTrainingPlanId(Integer trainingPlanId);
}