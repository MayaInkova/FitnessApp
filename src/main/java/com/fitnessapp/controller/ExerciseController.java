package com.fitnessapp.controller;

import com.fitnessapp.model.Exercise;
import com.fitnessapp.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    @Autowired
    private ExerciseRepository exerciseRepository;

    @GetMapping("/by-plan/{planId}")
    public List<Exercise> getExercisesByPlan(@PathVariable Integer planId) {
        return exerciseRepository.findByTrainingPlanId(planId);
    }
}