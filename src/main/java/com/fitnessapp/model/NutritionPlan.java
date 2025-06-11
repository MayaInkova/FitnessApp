package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
    private Integer id; // Променено от Long на Integer за консистентност

    @OneToOne(fetch = FetchType.LAZY) // Променено на OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true) // user_id е уникален за всеки план
    private User user;

    @Column(nullable = false)
    private LocalDate dateGenerated;

    private Double targetCalories;

    @Column(name = "protein")
    private Double protein;

    @Column(name = "fat")
    private Double fat;

    @Column(name = "carbohydrates")
    private Double carbohydrates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    @OneToMany(mappedBy = "nutritionPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Meal> meals = new ArrayList<>();

    public void addMeal(Meal meal) {
        // Проверката "if (this.meals == null)" е излишна заради @Builder.Default
        this.meals.add(meal);
        meal.setNutritionPlan(this);
    }

    public void removeMeal(Meal meal) {
        if (this.meals != null) {
            this.meals.remove(meal);
            meal.setNutritionPlan(null);
        }
    }
}