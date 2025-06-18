package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "food_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FoodItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    private String name;

    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private String unit;
    private Double quantity;
}