package com.fitnessapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    private String type;
    private String time;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    @JsonBackReference
    private NutritionPlan nutritionPlan;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;
}