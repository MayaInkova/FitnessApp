package com.fitnessapp.service;

import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.repository.NutritionPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NutritionPlanService {

    private final NutritionPlanRepository nutritionPlanRepository;

    @Autowired
    public NutritionPlanService(NutritionPlanRepository nutritionPlanRepository) {
        this.nutritionPlanRepository = nutritionPlanRepository;
    }

    public NutritionPlan savePlan(NutritionPlan plan) {
        return nutritionPlanRepository.save(plan);
    }

    public NutritionPlan getPlanByUserId(Long userId) {
        return nutritionPlanRepository.findByUserId(userId);
    }
    public List<NutritionPlan> getAllPlans() {
        return nutritionPlanRepository.findAll();
    }
}