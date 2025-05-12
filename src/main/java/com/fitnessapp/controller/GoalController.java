package com.fitnessapp.controller;


import com.fitnessapp.dto.GoalRequest;
import com.fitnessapp.model.Goal;
import com.fitnessapp.service.GoalService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping("/get")
    public Goal getGoal(@RequestBody GoalRequest request) {
        return goalService.getGoalByInput(request.getGoal());
    }

    @GetMapping("/get")
    public Goal getGoal(@RequestParam String goalInput) {
        return goalService.getGoalByInput(goalInput);
    }
}