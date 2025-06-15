package com.fitnessapp.controller;

import com.fitnessapp.dto.TrainingPlanDTO; // Added import for TrainingPlanDTO
import com.fitnessapp.service.TrainingPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // For better response handling
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/training")
@CrossOrigin(origins = "*")
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    @Autowired
    public TrainingPlanController(TrainingPlanService trainingPlanService) {
        this.trainingPlanService = trainingPlanService;
    }


    @GetMapping("/all")
    public ResponseEntity<List<TrainingPlanDTO>> getAll() {
        List<TrainingPlanDTO> plans = trainingPlanService.getAllTrainingPlansDTO();
        return ResponseEntity.ok(plans);
    }


    @GetMapping("/recommend")
    public ResponseEntity<TrainingPlanDTO> recommend(@RequestParam String goal, @RequestParam boolean withWeights) {
        TrainingPlanDTO recommendedPlan = trainingPlanService.getRecommendedTrainingPlanDTO(goal, withWeights);
        if (recommendedPlan == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(recommendedPlan);
    }
}
