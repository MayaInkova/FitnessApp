package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "food_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(name = "calories_per_100g")
    private Double caloriesPer100g;

    private Double proteins;
    private Double carbs;
    private Double fats;
}
