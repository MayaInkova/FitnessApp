package com.fitnessapp.service;

import com.fitnessapp.model.Meal;
import com.fitnessapp.repository.MealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MealService {

    private final MealRepository mealRepository;

    @Autowired
    public MealService(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    public Meal saveMeal(Meal meal) {
        return mealRepository.save(meal);
    }

    public List<Meal> getAllMeals() {
        return mealRepository.findAll();
    }

    public List<Meal> getMealsByPlanId(Long planId) {
        return mealRepository.findByPlanId(planId);
    }

    public void deleteMeal(Long id) {
        mealRepository.deleteById(id);
    }
}