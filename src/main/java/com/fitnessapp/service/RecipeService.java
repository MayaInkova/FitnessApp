package com.fitnessapp.service;

import com.fitnessapp.dto.RecipeDTO;
import com.fitnessapp.model.Recipe;
import com.fitnessapp.model.MealType;
import com.fitnessapp.model.DietType;
import com.fitnessapp.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    @Autowired
    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public Optional<Recipe> getRecipeById(Integer id) {
        return recipeRepository.findById(id);
    }

    public List<Recipe> getRecipesByMealType(MealType mealType) {
        return recipeRepository.findByMealType(mealType);
    }


    public List<RecipeDTO> getAllRecipeDTOs() {
        return recipeRepository.findAll().stream()
                .map(this::convertToRecipeDTO) // Използваме помощния метод за конверсия
                .collect(Collectors.toList());
    }


    public Optional<RecipeDTO> getRecipeDTOById(Integer id) {
        return recipeRepository.findById(id)
                .map(this::convertToRecipeDTO);
    }


    public List<RecipeDTO> getRecipeDTOsByMealType(MealType mealType) {
        return recipeRepository.findByMealType(mealType).stream()
                .map(this::convertToRecipeDTO)
                .collect(Collectors.toList());
    }

    public Recipe saveRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    public Recipe updateRecipe(Integer id, Recipe updatedRecipe) {
        return recipeRepository.findById(id).map(recipe -> {
            recipe.setName(updatedRecipe.getName());
            recipe.setDescription(updatedRecipe.getDescription());
            recipe.setCalories(updatedRecipe.getCalories());
            recipe.setProtein(updatedRecipe.getProtein());
            recipe.setCarbs(updatedRecipe.getCarbs());
            recipe.setFat(updatedRecipe.getFat());
            recipe.setIsVegetarian(updatedRecipe.getIsVegetarian());
            recipe.setContainsDairy(updatedRecipe.getContainsDairy());
            recipe.setContainsNuts(updatedRecipe.getContainsNuts());
            recipe.setContainsFish(updatedRecipe.getContainsFish());
            recipe.setContainsPork(updatedRecipe.getContainsPork());
            recipe.setTags(updatedRecipe.getTags());
            recipe.setMealType(updatedRecipe.getMealType());
            recipe.setInstructions(updatedRecipe.getInstructions());
            recipe.setAllergens(updatedRecipe.getAllergens());
            recipe.setMeatType(updatedRecipe.getMeatType());

            if (updatedRecipe.getDietType() != null) {

                recipe.setDietType(updatedRecipe.getDietType());
            }

            return recipeRepository.save(recipe);
        }).orElseThrow(() -> new RuntimeException("Рецепта с ID " + id + " не е намерена."));
    }

    public void deleteRecipe(Integer id) {
        recipeRepository.deleteById(id);
    }


    private RecipeDTO convertToRecipeDTO(Recipe recipe) {
        if (recipe == null) {
            return null;
        }
        return RecipeDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .description(recipe.getDescription())
                .calories(recipe.getCalories())
                .protein(recipe.getProtein())
                .carbs(recipe.getCarbs())
                .fat(recipe.getFat())
                .isVegetarian(recipe.getIsVegetarian())
                .containsDairy(recipe.getContainsDairy())
                .containsNuts(recipe.getContainsNuts())
                .containsFish(recipe.getContainsFish())
                .containsPork(recipe.getContainsPork())
                .tags(recipe.getTags())
                .mealType(recipe.getMealType())
                .instructions(recipe.getInstructions())
                .dietTypeName(recipe.getDietType() != null ? recipe.getDietType().getName() : null)
                .allergens(recipe.getAllergens())
                .meatType(recipe.getMeatType())
                .build();
    }
    public List<RecipeDTO> findAlternatives(MealType mealType, Integer excludeId) {
        return recipeRepository
                .findByMealTypeAndIdNot(mealType, excludeId)
                .stream()
                .map(this::convertToRecipeDTO)
                .limit(20)
                .toList();
    }

}