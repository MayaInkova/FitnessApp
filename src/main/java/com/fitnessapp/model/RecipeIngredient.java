package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;


@Entity
@Table(name = "recipe_ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "food_item_id")
    private FoodItem foodItem;

    @Column(name = "quantity_grams")
    private Double quantityGrams;

}
