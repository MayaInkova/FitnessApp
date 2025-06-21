package com.fitnessapp.dto;

// Променете импорта от MeatType към MeatPreferenceType
import com.fitnessapp.model.MeatPreferenceType; // <-- ТОЗИ ИМПОРТ Е ПРАВИЛНИЯТ!
import com.fitnessapp.model.MealType; // Този импорт е ОК

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Collections;
import java.util.List;
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
    private MeatPreferenceType meatType;

    @Builder.Default
    private List<RecipeIngredientDTO> ingredients = Collections.emptyList();

}