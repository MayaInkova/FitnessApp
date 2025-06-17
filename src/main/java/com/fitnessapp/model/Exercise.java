package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter; // Added
import lombok.Setter; // Added
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; // Added

@Entity
@Table(name = "exercises")
@Getter // Generates getters
@Setter // Generates setters
@NoArgsConstructor
@AllArgsConstructor
@Builder
// --- IMPORTANT CHANGE: Control EqualsAndHashCode ---
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Generates equals/hashCode only for fields with @EqualsAndHashCode.Include
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Include 'id' in equals() and hashCode()
    private Integer id;

    @Column(nullable = false)
    @EqualsAndHashCode.Include // You can include 'name' here if you consider it unique enough for equals/hashCode
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer sets;
    private Integer reps;
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    private ExerciseType type;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Enumerated(EnumType.STRING)
    private EquipmentType equipment;

    // LAZY ManyToOne relationship - DO NOT INCLUDE IN equals/hashCode
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_session_id")
    private TrainingSession trainingSession;
}