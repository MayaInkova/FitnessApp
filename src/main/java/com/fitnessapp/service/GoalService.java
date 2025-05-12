package com.fitnessapp.service;

import com.fitnessapp.model.Goal;
import com.fitnessapp.model.GoalType;
import com.fitnessapp.repository.GoalRepository;
import org.springframework.stereotype.Service;

@Service
public class GoalService {

    private final GoalRepository goalRepository;

    //  конструктор за dependency injection
    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public Goal getGoalByInput(String input) {
        String normalized = input.toLowerCase(); // напр. "maintain"
        GoalType goalType = GoalType.fromString(normalized);

        // Превръщаме обратно в string, защото в DB goal.name e на български
        return goalRepository.findByName(goalType.getDisplayName())
                .orElseThrow(() -> new RuntimeException("Goal not found"));
    }
}