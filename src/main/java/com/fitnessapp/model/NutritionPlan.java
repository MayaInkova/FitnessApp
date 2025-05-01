package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.Builder;
import java.util.List;

@Builder
@Entity
@Table(name = "nutrition_plans")

public class NutritionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbs;
    private String goal; // Например: "Отслабване", "Покачване на мускулна маса"

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Добавяне на ManyToMany връзка с Recipe
    @ManyToMany
    @JoinTable(
            name = "nutrition_plan_recipes",
            joinColumns = @JoinColumn(name = "nutrition_plan_id"),
            inverseJoinColumns = @JoinColumn(name = "recipe_id")
    )
    private List<Recipe> recipes; // Списък от рецепти

    // Празен конструктор
    public NutritionPlan() {}

    // Пълен конструктор
    public NutritionPlan(Long id, Double calories, Double protein, Double fat, Double carbs, String goal, User user, List<Recipe> recipes) {
        this.id = id;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.goal = goal;
        this.user = user;
        this.recipes = recipes;
    }

    // GETTERS
    public Long getId() {
        return id;
    }

    public Double getCalories() {
        return calories;
    }

    public Double getProtein() {
        return protein;
    }

    public Double getFat() {
        return fat;
    }

    public Double getCarbs() {
        return carbs;
    }

    public String getGoal() {
        return goal;
    }

    public User getUser() {
        return user;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    // SETTERS
    public void setId(Long id) {
        this.id = id;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public void setProtein(Double protein) {
        this.protein = protein;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }

    public void setCarbs(Double carbs) {
        this.carbs = carbs;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }
}