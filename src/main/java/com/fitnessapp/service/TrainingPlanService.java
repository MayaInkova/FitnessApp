package com.fitnessapp.service;

import com.fitnessapp.model.Exercise;
import com.fitnessapp.model.TrainingPlan;
import com.fitnessapp.repository.ExerciseRepository;
import com.fitnessapp.repository.TrainingPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;

    @Autowired
    public TrainingPlanService(TrainingPlanRepository trainingPlanRepository) {
        this.trainingPlanRepository = trainingPlanRepository;
    }

    @Autowired
    private ExerciseRepository exerciseRepository;

    public List<Exercise> getExercisesForPlan(Integer trainingPlanId) {
        return exerciseRepository.findByTrainingPlanId(trainingPlanId);
    }

    public List<TrainingPlan> getAll() {
        return trainingPlanRepository.findAll();
    }

    public TrainingPlan getRecommended(String goal, boolean withWeights) {
        return trainingPlanRepository
                .findFirstByGoalAndWithWeights(goal, withWeights)
                .orElse(null);
    }

    public TrainingPlan save(TrainingPlan plan) {
        return trainingPlanRepository.save(plan);
    }
}