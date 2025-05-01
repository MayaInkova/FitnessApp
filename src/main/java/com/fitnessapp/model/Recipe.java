package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbs;

    private String type; // Пример: "breakfast", "lunch", "dinner", "snack"
}
