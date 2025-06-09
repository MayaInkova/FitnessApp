package com.fitnessapp.service;

import com.fitnessapp.model.Exercise;
import com.fitnessapp.model.TrainingPlan;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.ExerciseRepository;
import com.fitnessapp.repository.TrainingPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final ExerciseRepository exerciseRepository;

    @Autowired
    public TrainingPlanService(TrainingPlanRepository trainingPlanRepository,
                               ExerciseRepository exerciseRepository) {
        this.trainingPlanRepository = trainingPlanRepository;
        this.exerciseRepository = exerciseRepository;
    }

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
    public TrainingPlan findMatchingPlan(User user) {
        boolean withWeights = user.getTrainingType() != null && user.getTrainingType().toLowerCase().contains("тежест");
        return getRecommended(user.getGoal(), withWeights);
    }
}