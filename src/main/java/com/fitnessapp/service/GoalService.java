package com.fitnessapp.service;

import com.fitnessapp.model.Goal;
import com.fitnessapp.model.GoalType;
import com.fitnessapp.repository.GoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;

    @Autowired
    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public Goal getGoalByInput(String input) {
        String normalized = input.toLowerCase();
        GoalType goalType = GoalType.fromString(normalized);

        // Използваме findByNameIgnoreCase, за да сме сигурни, че намираме целта
        return goalRepository.findByNameIgnoreCase(goalType.getDisplayName()) // Използвайте findByNameIgnoreCase
                .orElseThrow(() -> new RuntimeException("Goal not found"));
    }

    public List<Goal> getAllGoals() {
        return goalRepository.findAll();
    }

    public Goal updateGoal(Integer id, Goal updatedGoal) {
        Goal existing = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Целта не е намерена")); // Предупреждение за типо

        existing.setName(updatedGoal.getName());
        existing.setDescription(updatedGoal.getDescription());
        // Добавете .setCalorieModifier(), ако го имате в DTO и е необходимо
        if (updatedGoal.getCalorieModifier() != null) {
            existing.setCalorieModifier(updatedGoal.getCalorieModifier());
        }
        return goalRepository.save(existing);
    }

    public void deleteGoal(Integer id) {
        goalRepository.deleteById(id);
    }
}