package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder; // <-- Добави този import!
import lombok.Data; // Или Getters/Setters, ако използваш само тях
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diet_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;

    // Добави описание, ако има такова, за да е консистентно с DataInitializer-а
    private String description;
}