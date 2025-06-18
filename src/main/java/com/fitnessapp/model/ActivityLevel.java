package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter; // Добавено
import lombok.Setter; // Добавено
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; // Добавено

@Entity
@Table(name = "activity_levels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ActivityLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(unique = true, nullable = false)
    @EqualsAndHashCode.Include
    private String name;
    private String description;
    private Double multiplier;
}