package com.fitnessapp.service;

import com.fitnessapp.dto.FullPlanDTO;
import com.fitnessapp.model.*;
import com.fitnessapp.repository.MealRepository;
import com.fitnessapp.repository.NutritionPlanRepository;
import com.fitnessapp.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class NutritionPlanService {

    private final NutritionPlanRepository nutritionPlanRepository;
    private final RecipeRepository recipeRepository;
    private final MealRepository mealRepository;
    private final TrainingPlanService trainingPlanService;

    @Autowired
    public NutritionPlanService(NutritionPlanRepository nutritionPlanRepository,
                                RecipeRepository recipeRepository,
                                MealRepository mealRepository,
                                TrainingPlanService trainingPlanService) {
        this.nutritionPlanRepository = nutritionPlanRepository;
        this.recipeRepository = recipeRepository;
        this.mealRepository = mealRepository;
        this.trainingPlanService = trainingPlanService;
    }

    public NutritionPlan generatePlanForUser(User user, String dietType) {
        NutritionPlan plan = Optional.ofNullable(
                nutritionPlanRepository.findTopByUserIdOrderByIdDesc(user.getId())
        ).orElse(new NutritionPlan());

        plan.setUser(user);
        plan.setGoal(user.getGoal());

        double calories = NutritionCalculator.calculateTDEE(user);
        double protein = calories * 0.3 / 4;
        double fat = calories * 0.25 / 9;
        double carbs = (calories - (protein * 4 + fat * 9)) / 4;

        plan.setCalories(calories);
        plan.setProtein(protein);
        plan.setFat(fat);
        plan.setCarbs(carbs);

        if (plan.getMeals() == null) plan.setMeals(new ArrayList<>());
        else plan.getMeals().clear();

        if (plan.getRecipes() == null) plan.setRecipes(new ArrayList<>());
        else plan.getRecipes().clear();

        List<Recipe> allRecipes = recipeRepository.findAll();
        List<Recipe> filteredRecipes = filterRecipes(allRecipes, user, dietType);

        if (filteredRecipes.isEmpty()) {
            throw new IllegalStateException("Няма подходящи рецепти за зададените предпочитания и диетичен тип.");
        }

        int recipeIndex = 0;
        for (String day : List.of("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")) {
            for (String type : List.of("breakfast", "snack", "lunch", "dinner")) {
                Recipe recipe = filteredRecipes.get(recipeIndex % filteredRecipes.size());

                Meal meal = Meal.builder()
                        .dayOfWeek(day)
                        .type(type)
                        .time(getDefaultTimeForType(type).toString())
                        .recipe(recipe)
                        .nutritionPlan(plan)
                        .build();

                plan.addMeal(meal);
                plan.addRecipe(recipe);
                recipeIndex++;
            }
        }

        TrainingPlan training = trainingPlanService.findMatchingPlan(user);
        plan.setTrainingPlan(training);

        return nutritionPlanRepository.save(plan);
    }

    private List<Recipe> filterRecipes(List<Recipe> recipes, User user, String dietType) {
        final List<String> allergens = (user.getAllergies() != null && !user.getAllergies().isBlank() && !user.getAllergies().equalsIgnoreCase("не"))
                ? Arrays.asList(user.getAllergies().toLowerCase().split("[,;\\s]+"))
                : Collections.emptyList();

        boolean isVegan = "vegan".equalsIgnoreCase(dietType);

        return recipes.stream()
                .filter(r -> {
                    String ingredients = Optional.ofNullable(r.getIngredients()).orElse("").toLowerCase();
                    String description = Optional.ofNullable(r.getDescription()).orElse("").toLowerCase();
                    String recipeType = Optional.ofNullable(r.getType()).orElse("").toLowerCase();

                    // Match по тип диета
                    boolean matchesDiet = (dietType == null || dietType.isBlank())
                            || ingredients.contains(dietType.toLowerCase())
                            || description.contains(dietType.toLowerCase());

                    // Допълнително условие за веган
                    if (isVegan) {
                        if (ingredients.contains("месо") || ingredients.contains("пилешко") || ingredients.contains("риба")
                                || ingredients.contains("телешко") || ingredients.contains("яйца")
                                || ingredients.contains("сирене") || ingredients.contains("мляко")
                                || ingredients.contains("кисело мляко") || ingredients.contains("масло")) {
                            return false;
                        }
                    }

                    // Месо – важи само за lunch/dinner и ако НЕ е vegan
                    boolean isMainMeal = recipeType.equals("lunch") || recipeType.equals("dinner");
                    boolean matchesMeat = isVegan || user.getMeatPreference() == null || !isMainMeal
                            || ingredients.contains(user.getMeatPreference().toLowerCase());

                    // Млечни продукти
                    boolean matchesDairy = user.getConsumesDairy() == null || user.getConsumesDairy()
                            || (!ingredients.contains("мляко") && !ingredients.contains("сирене")
                            && !ingredients.contains("кисело мляко") && !ingredients.contains("масло"));

                    // Алергии
                    boolean matchesAllergies = allergens.stream().noneMatch(ingredients::contains);

                    return matchesDiet && matchesMeat && matchesDairy && matchesAllergies;
                })
                .toList();
    }

    private LocalDateTime getDefaultTimeForType(String type) {
        return switch (type.toLowerCase()) {
            case "breakfast" -> LocalDateTime.now().withHour(8).withMinute(0);
            case "snack" -> LocalDateTime.now().withHour(10).withMinute(30);
            case "lunch" -> LocalDateTime.now().withHour(13).withMinute(0);
            case "dinner" -> LocalDateTime.now().withHour(19).withMinute(30);
            default -> LocalDateTime.now();
        };
    }

    public NutritionPlan savePlan(NutritionPlan plan) {
        return nutritionPlanRepository.save(plan);
    }

    @Transactional
    public NutritionPlan getPlanByUserId(Integer userId) {
        NutritionPlan plan = nutritionPlanRepository.findTopByUserIdOrderByIdDesc(userId);
        if (plan != null) {
            plan.getMeals().size();
            plan.getRecipes().size();
            if (plan.getTrainingPlan() != null) {
                plan.getTrainingPlan().getName();
                plan.getTrainingPlan().getExercises().size();
            }
        }
        return plan;
    }

    @Transactional
    public FullPlanDTO getFullPlanByUserId(Integer userId) {
        NutritionPlan plan = getPlanByUserId(userId);
        if (plan == null) return null;

        TrainingPlan trainingPlan = plan.getTrainingPlan();

        return FullPlanDTO.builder()
                .calories(plan.getCalories())
                .protein(plan.getProtein())
                .fat(plan.getFat())
                .carbs(plan.getCarbs())
                .goal(plan.getGoal())
                .meals(plan.getMeals())
                .recipes(plan.getRecipes())
                .trainingName(trainingPlan != null ? trainingPlan.getName() : null)
                .trainingDescription(trainingPlan != null ? trainingPlan.getDescription() : null)
                .daysPerWeek(trainingPlan != null ? trainingPlan.getDaysPerWeek() : 0)
                .durationMinutes(trainingPlan != null ? trainingPlan.getDurationMinutes() : 0)
                .exercises(trainingPlan != null ? trainingPlan.getExercises() : Collections.emptyList())
                .build();
    }

    public FullPlanDTO getFullPlanForUser(Long userId) {
        return getFullPlanByUserId(userId.intValue());
    }

    public List<NutritionPlan> generateWeeklyPlanForUser(User user, String dietType) {
        List<NutritionPlan> plans = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            plans.add(generatePlanForUser(user, dietType));
        }
        return plans;
    }

    public List<NutritionPlan> getAllPlans() {
        return nutritionPlanRepository.findAll();
    }

    public List<NutritionPlan> getAllByUserId(Integer userId) {
        return nutritionPlanRepository.findAllByUserIdOrderByIdDesc(userId);
    }

    @Transactional
    public int fixMissingTrainingPlans() {
        List<NutritionPlan> plansWithoutTraining = nutritionPlanRepository.findAll()
                .stream()
                .filter(p -> p.getTrainingPlan() == null && p.getUser() != null)
                .toList();

        int updatedCount = 0;
        for (NutritionPlan plan : plansWithoutTraining) {
            User user = plan.getUser();
            boolean prefersWeights = user.getTrainingType() != null && user.getTrainingType().toLowerCase().contains("тежест");
            TrainingPlan trainingPlan = trainingPlanService.getRecommended(user.getGoal(), prefersWeights);
            if (trainingPlan != null) {
                plan.setTrainingPlan(trainingPlan);
                nutritionPlanRepository.save(plan);
                updatedCount++;
            }
        }
        return updatedCount;
    }
}
