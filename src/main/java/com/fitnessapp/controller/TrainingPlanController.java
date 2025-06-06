package com.fitnessapp.controller;

import com.fitnessapp.model.TrainingPlan;
import com.fitnessapp.service.TrainingPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training")
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    @Autowired
    public TrainingPlanController(TrainingPlanService trainingPlanService) {
        this.trainingPlanService = trainingPlanService;
    }

    @GetMapping("/all")
    public List<TrainingPlan> getAll() {
        return trainingPlanService.getAll();
    }

    @GetMapping("/recommend")
    public TrainingPlan recommend(@RequestParam String goal, @RequestParam boolean withWeights) {
        return trainingPlanService.getRecommended(goal, withWeights);
    }
}