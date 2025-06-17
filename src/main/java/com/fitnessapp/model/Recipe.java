package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter; // Added
import lombok.Setter; // Added
import lombok.EqualsAndHashCode; // Added

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "recipes")
@Getter // Generates getters
@Setter // Generates setters
@NoArgsConstructor
@AllArgsConstructor
@Builder
// --- ВАЖНА ПРОМЯНА: Контролираме EqualsAndHashCode ---
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Generates equals/hashCode only for fields with @EqualsAndHashCode.Include
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Include 'id' in equals() and hashCode()
    private Integer id;

    // Можете да добавите @EqualsAndHashCode.Include към name, ако е уникално
    private String name;
    private String description;
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

    // Collections: Even if EAGER, DO NOT INCLUDE THEM in equals/hashCode.
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
    // @EqualsAndHashCode.Exclude is redundant when using onlyExplicitlyIncluded = true,
    // but leaving it doesn't hurt. The key is it's NOT @EqualsAndHashCode.Include.
    private DietType dietType;

    // Collections: Even if EAGER, DO NOT INCLUDE THEM in equals/hashCode.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_allergens", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "allergen")
    @Builder.Default
    private Set<String> allergens = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private MeatType meatType;
}