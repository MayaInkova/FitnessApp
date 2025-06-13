package com.fitnessapp.service;

import com.fitnessapp.dto.FullPlanDTO;
import com.fitnessapp.dto.MealDTO;
import com.fitnessapp.dto.RecipeDTO;
import com.fitnessapp.dto.TrainingSessionDTO;
import com.fitnessapp.dto.ExerciseDTO;
import com.fitnessapp.dto.NutritionPlanDTO;
import com.fitnessapp.dto.TrainingPlanDTO;

import com.fitnessapp.model.ActivityLevel;
import com.fitnessapp.model.DietType;
import com.fitnessapp.model.GenderType;
import com.fitnessapp.model.GoalType;
import com.fitnessapp.model.MealFrequencyPreferenceType;
import com.fitnessapp.model.MealType;
import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.Recipe;
import com.fitnessapp.model.TrainingPlan;
import com.fitnessapp.model.TrainingSession;
import com.fitnessapp.model.Exercise;
import com.fitnessapp.model.User;
import com.fitnessapp.model.MeatType;
import com.fitnessapp.model.MeatPreferenceType;
import com.fitnessapp.model.OtherDietaryPreference;

import com.fitnessapp.repository.NutritionPlanRepository;
import com.fitnessapp.repository.RecipeRepository;
import com.fitnessapp.repository.TrainingPlanRepository;
import com.fitnessapp.repository.UserRepository;
import com.fitnessapp.service.NutritionCalculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NutritionPlanService {

    private static final Logger logger = LoggerFactory.getLogger(NutritionPlanService.class);

    private final NutritionPlanRepository nutritionPlanRepository;
    private final RecipeRepository recipeRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final UserRepository userRepository;
    private final TrainingPlanService trainingPlanService;

    @Autowired
    public NutritionPlanService(NutritionPlanRepository nutritionPlanRepository,
                                RecipeRepository recipeRepository,
                                TrainingPlanRepository trainingPlanRepository,
                                UserRepository userRepository,
                                TrainingPlanService trainingPlanService) {
        this.nutritionPlanRepository = nutritionPlanRepository;
        this.recipeRepository = recipeRepository;
        this.trainingPlanRepository = trainingPlanRepository;
        this.userRepository = userRepository;
        this.trainingPlanService = trainingPlanService;
    }

    @Transactional
    public NutritionPlan generateNutritionPlan(User user) {

        validateUserProfileForPlanGeneration(user);

        Optional<NutritionPlan> existingPlan =
                nutritionPlanRepository.findByUserAndDateGenerated(user, LocalDate.now());

        if (existingPlan.isPresent()) {
            logger.info("Existing nutrition plan found for user {}. Returning existing plan.", user.getFullName());
            Hibernate.initialize(existingPlan.get().getMeals());
            if (existingPlan.get().getMeals() != null) {
                existingPlan.get().getMeals().forEach(meal -> {
                    if (meal.getRecipe() != null) {
                        Hibernate.initialize(meal.getRecipe().getDietType());
                        Hibernate.initialize(meal.getRecipe().getAllergens());
                        Hibernate.initialize(meal.getRecipe().getTags());
                    }
                });
            }
            return existingPlan.get();
        }

        double targetCalories = NutritionCalculator.calculateTDEE(user);
        if (user.getGoal() != null && user.getGoal().getCalorieModifier() != null) {
            targetCalories += user.getGoal().getCalorieModifier();
        }
        logger.info("Calculated target calories for user {}: {}", user.getFullName(), targetCalories);


        List<Recipe> allRecipes = recipeRepository.findAll();
        logger.info("Total recipes in database: {}", allRecipes.size());
        List<Recipe> filteredRecipes = filterRecipes(allRecipes, user);
        logger.info("Filtered recipes count: {}", filteredRecipes.size());

        if (filteredRecipes.isEmpty()) {
            logger.warn("No recipes found matching user's preferences for user {}. Cannot generate nutrition plan.", user.getFullName());
            throw new IllegalStateException("Не са намерени рецепти, отговарящи на вашите предпочитания. Моля, проверете вашите диетични настройки.");
        }

        List<Recipe> breakfastRecipes = filteredRecipes.stream().filter(r -> r.getMealType() == MealType.BREAKFAST).collect(Collectors.toList());
        List<Recipe> lunchRecipes = filteredRecipes.stream().filter(r -> r.getMealType() == MealType.LUNCH).collect(Collectors.toList());
        List<Recipe> dinnerRecipes = filteredRecipes.stream().filter(r -> r.getMealType() == MealType.DINNER).collect(Collectors.toList());
        List<Recipe> snackRecipes = filteredRecipes.stream().filter(r -> r.getMealType() == MealType.SNACK).collect(Collectors.toList());

        logger.info("Breakfast recipes found after filtering: {}", breakfastRecipes.size());
        logger.info("Lunch recipes found after filtering: {}", lunchRecipes.size());
        logger.info("Dinner recipes found after filtering: {}", dinnerRecipes.size());
        logger.info("Snack recipes found after filtering: {}", snackRecipes.size());

        Recipe chosenBreakfast = breakfastRecipes.stream().findFirst().orElse(null);
        Recipe chosenLunch = lunchRecipes.stream().findFirst().orElse(null);
        Recipe chosenDinner = dinnerRecipes.stream().findFirst().orElse(null);
        Recipe chosenSnack1 = snackRecipes.stream().findFirst().orElse(null);
        Recipe chosenSnack2 = snackRecipes.size() > 1 ? snackRecipes.get(1) : null;


        logger.info("Chosen Breakfast: {}", chosenBreakfast != null ? chosenBreakfast.getName() : "None selected");
        logger.info("Chosen Lunch: {}", chosenLunch != null ? chosenLunch.getName() : "None selected");
        logger.info("Chosen Dinner: {}", chosenDinner != null ? chosenDinner.getName() : "None selected");
        logger.info("Chosen Snack 1: {}", chosenSnack1 != null ? chosenSnack1.getName() : "None selected");
        logger.info("Chosen Snack 2: {}", chosenSnack2 != null ? chosenSnack2.getName() : "None selected");


        NutritionPlan nutritionPlan = new NutritionPlan();
        nutritionPlan.setUser(user);
        nutritionPlan.setTargetCalories(targetCalories);
        nutritionPlan.setDateGenerated(LocalDate.now());

        List<com.fitnessapp.model.Meal> meals = new ArrayList<>();
        if (chosenBreakfast != null) {
            meals.add(com.fitnessapp.model.Meal.builder().recipe(chosenBreakfast).mealType(MealType.BREAKFAST).portionSize(1.0).build());
        }
        if (chosenLunch != null) {
            meals.add(com.fitnessapp.model.Meal.builder().recipe(chosenLunch).mealType(MealType.LUNCH).portionSize(1.0).build());
        }
        if (chosenDinner != null) {
            meals.add(com.fitnessapp.model.Meal.builder().recipe(chosenDinner).mealType(MealType.DINNER).portionSize(1.0).build());
        }

        if (user.getMealFrequencyPreference() != null && user.getMealFrequencyPreference() == MealFrequencyPreferenceType.FIVE_TIMES_DAILY) {
            if (chosenSnack1 != null) {
                meals.add(com.fitnessapp.model.Meal.builder().recipe(chosenSnack1).mealType(MealType.SNACK).portionSize(1.0).build());
            }
            if (chosenSnack2 != null) {
                meals.add(com.fitnessapp.model.Meal.builder().recipe(chosenSnack2).mealType(MealType.SNACK).portionSize(1.0).build());
            }
        }
        logger.info("Number of meals added to nutrition plan: {}", meals.size());
        nutritionPlan.setMeals(meals);

        double totalProtein = 0.0;
        double totalFat = 0.0;
        double totalCarbohydrates = 0.0;

        for (com.fitnessapp.model.Meal meal : meals) {
            if (meal.getRecipe() != null) {
                totalProtein += meal.getRecipe().getProtein() != null ? meal.getRecipe().getProtein() * meal.getPortionSize() : 0.0;
                totalFat += meal.getRecipe().getFat() != null ? meal.getRecipe().getFat() * meal.getPortionSize() : 0.0;
                totalCarbohydrates += meal.getRecipe().getCarbs() != null ? meal.getRecipe().getCarbs() * meal.getPortionSize() : 0.0;
            }
        }

        nutritionPlan.setProtein(totalProtein);
        nutritionPlan.setFat(totalFat);
        nutritionPlan.setCarbohydrates(totalCarbohydrates);
        logger.info("Calculated total macros for plan - Protein: {}, Fat: {}, Carbohydrates: {}", totalProtein, totalFat, totalCarbohydrates);

        user.setNutritionPlan(nutritionPlan);
        return nutritionPlanRepository.save(nutritionPlan);
    }


    private void validateUserProfileForPlanGeneration(User user) {
        List<String> missingFields = new ArrayList<>();
        if (user.getGender() == null) missingFields.add("пол");
        if (user.getAge() == null) missingFields.add("възраст");
        if (user.getHeight() == null) missingFields.add("ръст");
        if (user.getWeight() == null) missingFields.add("тегло");
        if (user.getActivityLevel() == null || user.getActivityLevel().getId() == null) missingFields.add("ниво на активност");
        if (user.getGoal() == null || user.getGoal().getId() == null) missingFields.add("цел");
        if (user.getDietType() == null || user.getDietType().getId() == null) missingFields.add("диетичен тип");
        if (user.getMeatPreference() == null) missingFields.add("предпочитания за месо");
        if (user.getConsumesDairy() == null) missingFields.add("консумира млечни продукти");


        if (!missingFields.isEmpty()) {
            String errorMessage = "Моля, попълнете всички задължителни данни за профила си (липсващи: " +
                    String.join(", ", missingFields) +
                    ") чрез чатбота или секция 'Профил', преди да генерирате план.";
            throw new IllegalArgumentException(errorMessage);
        }
    }


    private List<Recipe> filterRecipes(List<Recipe> allRecipes, User user) {
        logger.info("Starting recipe filtering for user: {}", user.getEmail());

        // Ensure lazy-loaded collections/entities are initialized for the user
        Hibernate.initialize(user.getAllergies());
        Hibernate.initialize(user.getOtherDietaryPreferences());
        Hibernate.initialize(user.getDietType());
        Hibernate.initialize(user.getGoal());
        Hibernate.initialize(user.getActivityLevel());

        MeatPreferenceType userMeatPreference = user.getMeatPreference();
        Set<String> userAllergies = Optional.ofNullable(user.getAllergies())
                .orElse(Collections.emptySet())
                .stream().map(String::toLowerCase).collect(Collectors.toSet());

        String userDietTypeName = user.getDietType() != null ? user.getDietType().getName().toLowerCase() : "";
        Boolean consumesDairy = user.getConsumesDairy();

        logger.info("User preferences summary for filtering: DietType={}, ConsumesDairy={}, MeatPreferenceType={}, Allergies={}, OtherDietaryPreferences={}",
                user.getDietType() != null ? user.getDietType().getName() : "N/A",
                user.getConsumesDairy(),
                user.getMeatPreference() != null ? user.getMeatPreference().name() : "N/A",
                userAllergies,
                user.getOtherDietaryPreferences());


        List<Recipe> filteredRecipes = allRecipes.stream()
                .filter(recipe -> {
                    boolean passesFilter = true;
                    StringBuilder reasons = new StringBuilder();

                    // Ensure lazy-loaded fields of the recipe are initialized
                    Hibernate.initialize(recipe.getDietType());
                    Hibernate.initialize(recipe.getAllergens());
                    Hibernate.initialize(recipe.getTags());


                    logger.debug("Evaluating recipe '{}' (ID: {}). Recipe details: DietType={}, MeatType={}, IsVegetarian={}, ContainsDairy={}, ContainsNuts={}, ContainsFish={}, Allergens={}, Tags={}",
                            recipe.getName(), recipe.getId(),
                            recipe.getDietType() != null ? recipe.getDietType().getName() : "N/A",
                            recipe.getMeatType() != null ? recipe.getMeatType().name() : "N/A",
                            recipe.getIsVegetarian(),
                            recipe.getContainsDairy(),
                            recipe.getContainsNuts(),
                            recipe.getContainsFish(),
                            recipe.getAllergens(),
                            recipe.getTags());


                    // 1. Filter by DietType (User's DietType must match recipe's DietType)
                    if (user.getDietType() != null) {
                        if ("vegetarian".equalsIgnoreCase(userDietTypeName)) {
                            if (!Boolean.TRUE.equals(recipe.getIsVegetarian())) {
                                passesFilter = false;
                                reasons.append("User is Vegetarian (DietType), but recipe '").append(recipe.getName()).append("' is not marked as vegetarian; ");
                            }
                        } else if ("vegan".equalsIgnoreCase(userDietTypeName)) {
                            if (!Boolean.TRUE.equals(recipe.getIsVegetarian()) ||
                                    Boolean.TRUE.equals(recipe.getContainsDairy()) ||
                                    Boolean.TRUE.equals(recipe.getContainsFish()) ||
                                    (recipe.getMeatType() != null && recipe.getMeatType() != MeatType.NONE)
                            ) {
                                passesFilter = false;
                                reasons.append("User is Vegan (DietType), but recipe '").append(recipe.getName()).append("' is not vegan (e.g., contains meat, dairy, or fish); ");
                            }
                        } else { // User has a specific diet type (e.g., "Standard", "Keto", "Paleo")
                            if (recipe.getDietType() != null && !recipe.getDietType().getName().equalsIgnoreCase(user.getDietType().getName())) {
                                // If recipe has a specific diet type and it doesn't match user's diet type, filter it.
                                passesFilter = false;
                                reasons.append("DietType mismatch for recipe '").append(recipe.getName()).append("' (user: ").append(user.getDietType().getName())
                                        .append(", recipe: ").append(recipe.getDietType().getName()).append("); ");
                            }
                            // If recipe.getDietType() is null, it is considered compatible with "Standard" or general diet types.
                            // If user is "Keto" and recipe.getDietType() is null, it will pass this part,
                            // but later parts might filter it based on macros/tags if you implement that.
                        }
                    }


                    // 2. Filter by MeatPreferenceType (User's specific meat exclusions vs. Recipe's MeatType)
                    if (userMeatPreference != null && userMeatPreference != MeatPreferenceType.NO_PREFERENCE) {
                        if (userMeatPreference == MeatPreferenceType.NONE) { // User explicitly wants no meat at all
                            if (recipe.getMeatType() != null && recipe.getMeatType() != MeatType.NONE) {
                                passesFilter = false;
                                reasons.append("User prefers NO_MEAT (MeatPreferenceType.NONE), but recipe '").append(recipe.getName()).append("' contains meat (").append(recipe.getMeatType()).append("); ");
                            }
                            // Also consider isVegetarian flag if user prefers no meat.
                            if (Boolean.TRUE.equals(recipe.getIsVegetarian()) == false) { // If recipe is not vegetarian, it's not meat-free.
                                passesFilter = false;
                                reasons.append("User prefers NO_MEAT, but recipe '").append(recipe.getName()).append("' is not marked as vegetarian; ");
                            }
                        } else {
                            if (recipe.getMeatType() != null) {
                                switch (userMeatPreference) {
                                    case PORK:
                                        if (recipe.getMeatType() == MeatType.PORK) {
                                            passesFilter = false;
                                            reasons.append("User prefers to avoid PORK, but recipe '").append(recipe.getName()).append("' contains PORK; ");
                                        }
                                        break;
                                    case BEEF:
                                        if (recipe.getMeatType() == MeatType.BEEF) {
                                            passesFilter = false;
                                            reasons.append("User prefers to avoid BEEF, but recipe '").append(recipe.getName()).append("' contains BEEF; ");
                                        }
                                        break;
                                    case CHICKEN:
                                        if (recipe.getMeatType() == MeatType.CHICKEN) {
                                            passesFilter = false;
                                            reasons.append("User prefers to avoid CHICKEN, but recipe '").append(recipe.getName()).append("' contains CHICKEN; ");
                                        }
                                        break;
                                    case FISH:
                                        if (recipe.getMeatType() == MeatType.FISH || Boolean.TRUE.equals(recipe.getContainsFish())) {
                                            passesFilter = false;
                                            reasons.append("User prefers to avoid FISH, but recipe '").append(recipe.getName()).append("' contains FISH; ");
                                        }
                                        break;
                                    case LAMB:
                                        if (recipe.getMeatType() == MeatType.LAMB) {
                                            passesFilter = false;
                                            reasons.append("User prefers to avoid LAMB, but recipe '").append(recipe.getName()).append("' contains LAMB; ");
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    }


                    // 3. Filter by Dairy preference
                    if (Boolean.FALSE.equals(consumesDairy) && Boolean.TRUE.equals(recipe.getContainsDairy())) {
                        passesFilter = false;
                        reasons.append("User does not consume dairy, but recipe '").append(recipe.getName()).append("' contains dairy; ");
                    }

                    // 4. Filter by Allergies (Check if recipe contains any of user's allergies)
                    if (!userAllergies.isEmpty() && recipe.getAllergens() != null && !recipe.getAllergens().isEmpty()) {
                        boolean hasAllergyConflict = recipe.getAllergens().stream()
                                .map(String::toLowerCase)
                                .anyMatch(userAllergies::contains);
                        if (hasAllergyConflict) {
                            passesFilter = false;
                            reasons.append("Recipe '").append(recipe.getName()).append("' contains user's allergy (Recipe allergens: ").append(recipe.getAllergens())
                                    .append(", User allergies: ").append(userAllergies).append("); ");
                        }
                    }

                    // 5. Filter by other dietary preferences (e.g., gluten-free, nut-free etc.)
                    if (user.getOtherDietaryPreferences() != null && !user.getOtherDietaryPreferences().isEmpty()) {
                        Set<OtherDietaryPreference> userOtherDietaryPreferencesEnums = user.getOtherDietaryPreferences().stream()
                                .map(s -> {
                                    try {
                                        return OtherDietaryPreference.valueOf(s.toUpperCase().replace("-", "_"));
                                    } catch (IllegalArgumentException e) {
                                        logger.warn("Unknown OtherDietaryPreference found in user profile: {}", s);
                                        return null;
                                    }
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toSet());


                        if (userOtherDietaryPreferencesEnums.contains(OtherDietaryPreference.GLUTEN_FREE) &&
                                (recipe.getTags() == null || !recipe.getTags().stream().map(String::toLowerCase).collect(Collectors.toSet()).contains("gluten-free"))) {
                            passesFilter = false;
                            reasons.append("User prefers GLUTEN_FREE, but recipe '").append(recipe.getName()).append("' is not tagged as gluten-free; ");
                        }
                        if (userOtherDietaryPreferencesEnums.contains(OtherDietaryPreference.NUT_FREE) && Boolean.TRUE.equals(recipe.getContainsNuts())) {
                            passesFilter = false;
                            reasons.append("User prefers NUT_FREE, but recipe '").append(recipe.getName()).append("' contains nuts; ");
                        }
                        if (userOtherDietaryPreferencesEnums.contains(OtherDietaryPreference.FISH_FREE) && Boolean.TRUE.equals(recipe.getContainsFish())) {
                            passesFilter = false;
                            reasons.append("User prefers FISH_FREE, but recipe '").append(recipe.getName()).append("' contains fish; ");
                        }
                    }

                    // Log the outcome for EACH recipe
                    if (!passesFilter) { // Log if it failed any filter
                        logger.debug("Filtering out recipe '{}' (ID: {}). Reasons: {}", recipe.getName(), recipe.getId(), reasons.toString());
                    } else {
                        logger.debug("Recipe '{}' (ID: {}) passed all filters.", recipe.getName(), recipe.getId());
                    }
                    return passesFilter;
                })
                .collect(Collectors.toList());

        logger.info("Finished recipe filtering. Total filtered recipes: {}", filteredRecipes.size());
        return filteredRecipes;
    }


    @Transactional
    public NutritionPlanDTO saveNutritionPlan(NutritionPlan plan) {
        if (plan.getUser() != null && plan.getUser().getId() != null) {
            User managedUser = userRepository.findById(plan.getUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException("User with ID " + plan.getUser().getId() + " not found."));
            plan.setUser(managedUser);
        } else if (plan.getUser() != null) {
            userRepository.save(plan.getUser());
        }
        NutritionPlan savedPlan = nutritionPlanRepository.save(plan);
        return convertNutritionPlanToDTO(savedPlan);
    }


    @Transactional(readOnly = true)
    public List<NutritionPlanDTO> getNutritionPlansByUserDTO(User user) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + user.getId() + " not found."));
        return nutritionPlanRepository.findByUser(managedUser).stream()
                .peek(plan -> Hibernate.initialize(plan.getMeals()))
                .map(this::convertNutritionPlanToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<NutritionPlanDTO> getAllNutritionPlansDTO() {
        return nutritionPlanRepository.findAll().stream()
                .peek(plan -> Hibernate.initialize(plan.getMeals()))
                .map(this::convertNutritionPlanToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public FullPlanDTO getFullPlanByUserId(Integer userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.warn("User with ID {} not found for full plan retrieval.", userId);
                return null;
            }

            Hibernate.initialize(user.getTrainingPlan());
            Hibernate.initialize(user.getNutritionPlan());

            List<NutritionPlan> nutritionPlans = nutritionPlanRepository.findByUser(user);
            NutritionPlan lastNutritionPlan = nutritionPlans.stream()
                    .max(Comparator.comparing(NutritionPlan::getDateGenerated))
                    .orElse(null);

            TrainingPlan trainingPlan = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user)
                    .stream().findFirst().orElse(null);

            if (lastNutritionPlan == null && trainingPlan == null) {
                logger.info("No nutrition or training plan found for user ID {}.", userId);
                return FullPlanDTO.builder().build();
            }

            List<MealDTO> mealDTOs = null;
            if (lastNutritionPlan != null) {
                Hibernate.initialize(lastNutritionPlan.getMeals());
                Hibernate.initialize(lastNutritionPlan.getGoal());

                if (lastNutritionPlan.getMeals() != null) {
                    mealDTOs = lastNutritionPlan.getMeals().stream()
                            .map(meal -> {
                                Hibernate.initialize(meal.getRecipe());
                                if (meal.getRecipe() != null) {
                                    Hibernate.initialize(meal.getRecipe().getDietType());
                                    Hibernate.initialize(meal.getRecipe().getAllergens());
                                    Hibernate.initialize(meal.getRecipe().getTags());
                                }
                                return this.convertMealToMealDTO(meal);
                            })
                            .collect(Collectors.toList());
                }
            }

            List<TrainingSessionDTO> trainingSessionDTOs = null;
            if (trainingPlan != null) {
                Hibernate.initialize(trainingPlan.getTrainingSessions());
                if (trainingPlan.getTrainingSessions() != null) {
                    trainingSessionDTOs = trainingPlan.getTrainingSessions().stream()
                            .map(session -> {
                                Hibernate.initialize(session.getExercises());
                                return this.convertTrainingSessionToTrainingSessionDTO(session);
                            })
                            .collect(Collectors.toList());
                }
            }

            return FullPlanDTO.builder()
                    .nutritionPlanId(lastNutritionPlan != null ? lastNutritionPlan.getId() : null)
                    .targetCalories(lastNutritionPlan != null ? lastNutritionPlan.getTargetCalories() : null)
                    .protein(lastNutritionPlan != null ? lastNutritionPlan.getProtein() : null)
                    .fat(lastNutritionPlan != null ? lastNutritionPlan.getFat() : null)
                    .carbohydrates(lastNutritionPlan != null ? lastNutritionPlan.getCarbohydrates() : null)
                    .goalName(lastNutritionPlan != null && lastNutritionPlan.getGoal() != null ? lastNutritionPlan.getGoal().getName() : null)
                    .meals(mealDTOs)
                    .trainingPlanId(trainingPlan != null ? trainingPlan.getId() : null)
                    .trainingPlanDescription(getTrainingSummary(user))
                    .trainingDaysPerWeek(trainingPlan != null ? trainingPlan.getDaysPerWeek() : null)
                    .trainingDurationMinutes(trainingPlan != null ? trainingPlan.getDurationMinutes() : null)
                    .trainingSessions(trainingSessionDTOs)
                    .build();

        } catch (Exception e) {
            logger.error("Error retrieving full plan for user ID: {}", userId, e);
            throw new RuntimeException("Failed to retrieve full plan for user: " + userId, e);
        }
    }


    public String getTrainingSummary(User user) {
        Optional<TrainingPlan> trainingPlanOpt = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user).stream().findFirst();

        if (trainingPlanOpt.isPresent()) {
            TrainingPlan plan = trainingPlanOpt.get();
            Hibernate.initialize(plan.getTrainingSessions());
            StringBuilder sb = new StringBuilder("Тренировъчен план за ")
                    .append(plan.getDateGenerated()).append(":\n")
                    .append("Дни: ").append(Optional.ofNullable(plan.getDaysPerWeek()).orElse(0)).append("\n")
                    .append("Продължителност: ").append(Optional.ofNullable(plan.getDurationMinutes()).orElse(0)).append(" мин\n\n");

            for (TrainingSession session : Optional.ofNullable(plan.getTrainingSessions()).orElse(Collections.emptyList())) {
                Hibernate.initialize(session.getExercises());
                sb.append("  ").append(Optional.ofNullable(session.getDayOfWeek()).map(Enum::name).orElse("Неизвестен ден")).append(" (")
                        .append(Optional.ofNullable(session.getDurationMinutes()).orElse(0)).append(" мин.):\n");
                for (Exercise exercise : Optional.ofNullable(session.getExercises()).orElse(Collections.emptyList())) {
                    sb.append("    - ").append(Optional.ofNullable(exercise.getName()).orElse("Неизвестно упражнение")).append(" (").append(Optional.ofNullable(exercise.getDescription()).orElse("")).append(")\n");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
        return "Няма намерен тренировъчен план.";
    }


    private RecipeDTO convertRecipeToRecipeDTO(Recipe recipe) {
        if (recipe == null) return null;
        Hibernate.initialize(recipe.getDietType());
        Hibernate.initialize(recipe.getAllergens());
        Hibernate.initialize(recipe.getTags());
        return RecipeDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .description(recipe.getDescription())
                .imageUrl(recipe.getImageUrl())
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

    private MealDTO convertMealToMealDTO(com.fitnessapp.model.Meal meal) {
        if (meal == null) return null;

        Hibernate.initialize(meal.getRecipe());

        RecipeDTO recipeDTO = convertRecipeToRecipeDTO(meal.getRecipe());
        return MealDTO.builder()
                .id(meal.getId())
                .recipe(recipeDTO)
                .mealType(meal.getMealType())
                .portionSize(meal.getPortionSize())
                .build();
    }


    private ExerciseDTO convertExerciseToExerciseDTO(Exercise exercise) {
        if (exercise == null) return null;
        return ExerciseDTO.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .description(exercise.getDescription())
                .sets(exercise.getSets())
                .reps(exercise.getReps())
                .durationMinutes(exercise.getDurationMinutes())
                .type(exercise.getType())
                .difficultyLevel(exercise.getDifficultyLevel())
                .equipment(exercise.getEquipment())
                .build();
    }


    private TrainingSessionDTO convertTrainingSessionToTrainingSessionDTO(TrainingSession session) {
        if (session == null) return null;
        List<ExerciseDTO> exerciseDTOs = null;

        Hibernate.initialize(session.getExercises());
        if (session.getExercises() != null) {
            exerciseDTOs = session.getExercises().stream()
                    .map(this::convertExerciseToExerciseDTO)
                    .collect(Collectors.toList());
        }
        return TrainingSessionDTO.builder()
                .id(session.getId())
                .dayOfWeek(session.getDayOfWeek())
                .durationMinutes(session.getDurationMinutes())
                .exercises(exerciseDTOs)
                .build();
    }


    public NutritionPlanDTO convertNutritionPlanToDTO(NutritionPlan plan) {
        if (plan == null) return null;

        Hibernate.initialize(plan.getUser());
        Hibernate.initialize(plan.getGoal());
        Hibernate.initialize(plan.getMeals());

        List<MealDTO> mealDTOs = null;
        if (plan.getMeals() != null) {
            mealDTOs = plan.getMeals().stream()
                    .map(this::convertMealToMealDTO)
                    .collect(Collectors.toList());
        }

        return NutritionPlanDTO.builder()
                .id(plan.getId())
                .dateGenerated(plan.getDateGenerated())
                .targetCalories(plan.getTargetCalories())
                .protein(plan.getProtein())
                .fat(plan.getFat())
                .carbohydrates(plan.getCarbohydrates())
                .goalName(plan.getGoal() != null ? plan.getGoal().getName() : null)
                .userId(plan.getUser() != null ? plan.getUser().getId() : null)
                .userEmail(plan.getUser() != null ? plan.getUser().getEmail() : null)
                .meals(mealDTOs)
                .build();
    }


    @Transactional
    public NutritionPlanDTO generateAndSaveNutritionPlanForUserDTO(User user) {
        NutritionPlan generatedPlan = generateNutritionPlan(user);
        return convertNutritionPlanToDTO(generatedPlan);
    }

    public void fixMissingTrainingPlans() {
        logger.info("Fixing missing training plans (implementation pending).");
    }

}