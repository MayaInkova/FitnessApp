package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    private String name;
    private String imageUrl;

    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private Boolean isVegetarian;
    private Boolean containsDairy;
    private Boolean containsNuts;
    private Boolean containsFish;
    private Boolean containsPork;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_tags", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private MealType mealType;

    private String instructions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_type_id")
    private DietType dietType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_allergens", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "allergen")
    @Builder.Default
    private Set<String> allergens = new HashSet<>();


    @Enumerated(EnumType.STRING)
    private MeatPreferenceType meatType;
}