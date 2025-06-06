package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diet_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;
}