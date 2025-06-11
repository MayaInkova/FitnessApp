package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "activity_levels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name; // Например: "Sedentary", "Light", "Moderate", "Active", "Very Active"
    private String description; // Кратко описание, ако е необходимо
    private Double multiplier; // Множител за калории (напр. 1.2 за Sedentary, 1.375 за Light и т.н.)
}