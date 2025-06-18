package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
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


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_session_id")
    private TrainingSession trainingSession;
}