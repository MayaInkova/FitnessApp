package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "meals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Meal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutrition_plan_id")
    private NutritionPlan nutritionPlan;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Enumerated(EnumType.STRING)
    private MealType mealType;

    private Double portionSize;

}