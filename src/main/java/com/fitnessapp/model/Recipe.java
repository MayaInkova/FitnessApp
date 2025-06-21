package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "recipes")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Recipe {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    private String  name;
    private String  imageUrl;

    private Double  calories;
    private Double  protein;
    private Double  carbs;
    private Double  fat;

    private Boolean isVegetarian;
    private Boolean containsDairy;
    private Boolean containsNuts;
    private Boolean containsFish;
    private Boolean containsPork;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private MealType mealType;

    private String instructions;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_type_id")
    private DietType dietType;

    @Enumerated(EnumType.STRING)
    private MeatPreferenceType meatType;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_tags", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_allergens", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "allergen")
    @Builder.Default
    private Set<String> allergens = new HashSet<>();


    @OneToMany(mappedBy = "recipe",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<RecipeIngredient> ingredients = new ArrayList<>();


    public void addIngredient(RecipeIngredient ri) {
        ingredients.add(ri);
        ri.setRecipe(this);
    }
    public void removeIngredient(RecipeIngredient ri) {
        ingredients.remove(ri);
        ri.setRecipe(null);
    }
}