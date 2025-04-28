package com.fitnessapp.model;


import jakarta.persistence.*;

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

    // Празен конструктор
    public NutritionPlan() {}

    // Пълен конструктор
    public NutritionPlan(Long id, Double calories, Double protein, Double fat, Double carbs, String goal, User user) {
        this.id = id;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.goal = goal;
        this.user = user;
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
}