package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.*;
import com.fitnessapp.model.MealType;

@Entity
@Table(name = "meals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meal meal = (Meal) o;
        return id != null && id.equals(meal.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}