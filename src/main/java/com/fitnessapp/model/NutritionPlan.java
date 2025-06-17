package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter; // Added
import lombok.Setter; // Added
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; // Added

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nutrition_plans", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "dateGenerated"})
})
@Getter // Generates getters
@Setter // Generates setters
@NoArgsConstructor
@AllArgsConstructor
@Builder
// --- IMPORTANT CHANGE: Control EqualsAndHashCode ---
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Generates equals/hashCode only for fields with @EqualsAndHashCode.Include
public class NutritionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Include 'id' in equals() and hashCode()
    private Integer id;

    // LAZY ManyToOne relationship - DO NOT INCLUDE IN equals/hashCode
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
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

    // LAZY ManyToOne relationship - DO NOT INCLUDE IN equals/hashCode
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    // OneToMany collection - DO NOT INCLUDE IN equals/hashCode
    @OneToMany(mappedBy = "nutritionPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Meal> meals = new ArrayList<>();

    // --- Helper methods for bidirectional relationships ---
    public void addMeal(Meal meal) {
        if (this.meals == null) { // Add null check for safety, though @Builder.Default should handle it
            this.meals = new ArrayList<>();
        }
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