package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fitnessapp.model.MealType;
import com.fitnessapp.model.MeatType;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeDTO {
    private Integer id;
    private String name;
    private String description;
    private String imageUrl;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;

    private Boolean isVegetarian;
    private Boolean containsDairy;
    private Boolean containsNuts;
    private Boolean containsFish;
    private Boolean containsPork;

    private Set<String> tags;
    private MealType mealType;
    private String instructions;
    private String dietTypeName;
    private Set<String> allergens;
    private MeatType meatType;
}
