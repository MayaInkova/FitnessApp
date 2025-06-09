package com.fitnessapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "recipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String type; // breakfast, lunch, dinner и т.н.

    @Lob
    @Column(length = 1000)
    private String instructions;

    private String imageUrl;

    private double calories;
    private double protein;
    private double fat;
    private double carbs;

    @Lob
    @Column(length = 1000)
    private String description;

    @Lob
    @Column(length = 1000)
    private String ingredients; // използва се за филтриране по месо, млечни и алергии

    @ManyToMany(mappedBy = "recipes")
    @JsonBackReference
    private List<NutritionPlan> nutritionPlans = new ArrayList<>();
}