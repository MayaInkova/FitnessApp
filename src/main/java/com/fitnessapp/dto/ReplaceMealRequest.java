package com.fitnessapp.dto;


public record ReplaceMealRequest(Integer originalMealId, Integer substituteRecipeId) {}
