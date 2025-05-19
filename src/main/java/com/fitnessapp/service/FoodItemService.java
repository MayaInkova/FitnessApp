package com.fitnessapp.service;

import com.fitnessapp.model.FoodItem;
import com.fitnessapp.repository.FoodItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoodItemService {

    private final FoodItemRepository foodItemRepository;

    @Autowired
    public FoodItemService(FoodItemRepository foodItemRepository) {
        this.foodItemRepository = foodItemRepository;
    }

    public FoodItem saveFoodItem(FoodItem item) {
        return foodItemRepository.save(item);
    }

    public List<FoodItem> getAllFoodItems() {
        return foodItemRepository.findAll();
    }

    public void deleteFoodItem(Integer id) {
        foodItemRepository.deleteById(id);
    }
}