package com.fitnessapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private String videoUrl;

    private double calories;
    private double protein;
    private double fat;
    private double carbs;

    @Lob
    @Column(length = 1000)
    private String description;


    //  Set<String> tags
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_tags", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @ManyToMany(mappedBy = "recipes")
    @JsonBackReference
    private List<NutritionPlan> nutritionPlans = new ArrayList<>();
}