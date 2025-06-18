package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String name;

    private String description;

    private Double calorieModifier;
}