package com.fitnessapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nutrition_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbs;
    private String goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"nutritionPlans", "trainingType", "dietType", "allergies"})
    private User user;

    @OneToMany(mappedBy = "nutritionPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("nutritionPlan")
    private List<Meal> meals = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "nutrition_plan_recipes",
            joinColumns = @JoinColumn(name = "nutrition_plan_id"),
            inverseJoinColumns = @JoinColumn(name = "recipe_id")
    )
    @JsonIgnoreProperties("nutritionPlans")
    private List<Recipe> recipes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_plan_id")
    @JsonIgnoreProperties("nutritionPlans")
    private TrainingPlan trainingPlan;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void addMeal(Meal meal) {
        if (this.meals == null) {
            this.meals = new ArrayList<>();
        }
        this.meals.add(meal);
        meal.setNutritionPlan(this); // за свързване обратно към плана
    }

    public void addRecipe(Recipe recipe) {
        if (this.recipes == null) {
            this.recipes = new ArrayList<>();
        }
        this.recipes.add(recipe);
    }
}