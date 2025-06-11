package com.fitnessapp.service;

import com.fitnessapp.model.MealItem;
import com.fitnessapp.repository.MealItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MealItemService {

    private final MealItemRepository mealItemRepository;

    @Autowired
    public MealItemService(MealItemRepository mealItemRepository) {
        this.mealItemRepository = mealItemRepository;
    }

    public MealItem saveMealItem(MealItem item) {
        return mealItemRepository.save(item);
    }

    public List<MealItem> getAllMealItems() {
        return mealItemRepository.findAll();
    }

    public List<MealItem> getMealItemsByMealId(Integer mealId) {
        // КОРИГИРАНО: Използвай findByMeal_Id, както е дефинирано в Repository
        return mealItemRepository.findByMeal_Id(mealId);
    }

    public void deleteMealItem(Integer id) {
        mealItemRepository.deleteById(id);
    }
}