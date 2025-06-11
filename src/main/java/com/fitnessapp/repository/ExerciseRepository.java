package com.fitnessapp.repository;

import com.fitnessapp.model.Exercise;
import com.fitnessapp.model.DifficultyLevel;
import com.fitnessapp.model.ExerciseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {

    List<Exercise> findByDifficultyLevelAndType(DifficultyLevel difficultyLevel, ExerciseType type);
    Optional<Exercise> findByNameIgnoreCase(String name);
    List<Exercise> findByType(ExerciseType type);
    List<Exercise> findByTrainingSession_TrainingPlan_Id(Integer trainingPlanId);
}