package com.fitnessapp.service;

import com.fitnessapp.model.Recipe;
import com.fitnessapp.model.MealType;
import com.fitnessapp.model.MeatPreferenceType;
import com.fitnessapp.model.DietType;
import com.fitnessapp.repository.RecipeRepository;
import com.fitnessapp.repository.DietTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final DietTypeRepository dietTypeRepository;

    @Autowired
    public RecipeService(RecipeRepository recipeRepository, DietTypeRepository dietTypeRepository) {
        this.recipeRepository = recipeRepository;
        this.dietTypeRepository = dietTypeRepository;
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public Optional<Recipe> getRecipeById(Integer id) {
        return recipeRepository.findById(id);
    }

    public List<Recipe> getRecipesByMealType(MealType mealType) {
        // ВАЖНО: Уверете се, че MealType в RecipeRepository е дефиниран правилно
        // List<Recipe> findByMealType(MealType mealType);
        return recipeRepository.findByMealType(mealType);
    }

    // Метод за търсене на рецепти по ИМЕ на диета (тъй като DietType е Entity)
    public List<Recipe> getRecipesByDietTypeName(String dietTypeName) {
        Optional<DietType> dietTypeOptional = dietTypeRepository.findByNameIgnoreCase(dietTypeName);
        if (dietTypeOptional.isPresent()) {
            // ВАЖНО: Уверете се, че RecipeRepository има този метод: List<Recipe> findByDietType(DietType dietType);
            return recipeRepository.findByDietType(dietTypeOptional.get());
        }
        return List.of(); // Връща празен списък, ако диетата не е намерена
    }

    @Transactional
    public Recipe saveRecipe(Recipe recipe) {
        // Уверете се, че DietType е управляван entity преди запазване на рецептата
        if (recipe.getDietType() != null && recipe.getDietType().getName() != null) {
            Optional<DietType> existingDietType = dietTypeRepository.findByNameIgnoreCase(recipe.getDietType().getName());
            if (existingDietType.isPresent()) {
                recipe.setDietType(existingDietType.get()); // Прикачи съществуващия entity
            } else {
                // Ако диетата не съществува в базата данни, може да я запазиш (ако е позволено)
                // или да хвърлиш изключение. Тук хвърляме изключение за яснота.
                throw new IllegalArgumentException("DietType '" + recipe.getDietType().getName() + "' не е намерена и не може да бъде създадена автоматично.");
                // Ако искаш да я създаваш автоматично, може да разкоментираш:
                // dietTypeRepository.save(recipe.getDietType());
            }
        }
        return recipeRepository.save(recipe);
    }

    @Transactional
    public Recipe updateRecipe(Integer id, Recipe updatedRecipe) {
        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Рецептата не е намерена"));

        // Актуализация на основните полета
        Optional.ofNullable(updatedRecipe.getName()).ifPresent(existingRecipe::setName);
        Optional.ofNullable(updatedRecipe.getDescription()).ifPresent(existingRecipe::setDescription);
        Optional.ofNullable(updatedRecipe.getImageUrl()).ifPresent(existingRecipe::setImageUrl);
        Optional.ofNullable(updatedRecipe.getCalories()).ifPresent(existingRecipe::setCalories);
        Optional.ofNullable(updatedRecipe.getProtein()).ifPresent(existingRecipe::setProtein);
        Optional.ofNullable(updatedRecipe.getCarbs()).ifPresent(existingRecipe::setCarbs);
        Optional.ofNullable(updatedRecipe.getFat()).ifPresent(existingRecipe::setFat);
        Optional.ofNullable(updatedRecipe.getInstructions()).ifPresent(existingRecipe::setInstructions);
        // Забележка: За Set-ове като tags, allergens, ако искате да замените, просто извикайте setTags(updatedRecipe.getTags()).
        // Ако updatedRecipe.getTags() е null, setTags може да направи existingRecipe.tags null, ако не се управлява от Builder.Default
        // Затова Optional.ofNullable е по-безопасен
        Optional.ofNullable(updatedRecipe.getTags()).ifPresent(existingRecipe::setTags);


        // Актуализация на MealType (enum)
        Optional.ofNullable(updatedRecipe.getMealType()).ifPresent(existingRecipe::setMealType);

        // Актуализация на DietType (entity)
        if (updatedRecipe.getDietType() != null) {
            Optional<DietType> dietTypeOptional = dietTypeRepository.findByNameIgnoreCase(updatedRecipe.getDietType().getName());
            if (dietTypeOptional.isPresent()) {
                existingRecipe.setDietType(dietTypeOptional.get()); // Прикачи съществуващия entity
            } else {
                throw new RuntimeException("DietType '" + updatedRecipe.getDietType().getName() + "' не е намерен.");
            }
        } else {
            existingRecipe.setDietType(null); // Ако диетата е премахната (или я остави непроменена, ако не искаш null)
        }

        // Актуализация на MeatType (enum)
        Optional.ofNullable(updatedRecipe.getMeatType()).ifPresent(existingRecipe::setMeatType);

        // Актуализация на булеви полета
        Optional.ofNullable(updatedRecipe.getContainsDairy()).ifPresent(existingRecipe::setContainsDairy);
        Optional.ofNullable(updatedRecipe.getContainsFish()).ifPresent(existingRecipe::setContainsFish);
        Optional.ofNullable(updatedRecipe.getContainsNuts()).ifPresent(existingRecipe::setContainsNuts);
        Optional.ofNullable(updatedRecipe.getContainsPork()).ifPresent(existingRecipe::setContainsPork);
        Optional.ofNullable(updatedRecipe.getIsVegetarian()).ifPresent(existingRecipe::setIsVegetarian);

        // Актуализация на списъци (алергени)
        Optional.ofNullable(updatedRecipe.getAllergens()).ifPresent(existingRecipe::setAllergens);


        return recipeRepository.save(existingRecipe);
    }

    public void deleteRecipe(Integer id) {
        recipeRepository.deleteById(id);
    }
}