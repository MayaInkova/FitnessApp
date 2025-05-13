package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String type; // breakfast, lunch, etc.

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private NutritionPlan nutritionPlan;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;
}