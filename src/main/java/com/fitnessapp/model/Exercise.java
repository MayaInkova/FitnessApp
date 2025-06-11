package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exercises")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer sets;
    private Integer reps;
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING) // Използваме enum за тип упражнение
    private ExerciseType type;

    @Enumerated(EnumType.STRING) // Използваме enum за ниво на трудност
    private DifficultyLevel difficultyLevel;

    @Enumerated(EnumType.STRING) // Използваме enum за оборудване
    private EquipmentType equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_session_id")
    private TrainingSession trainingSession;
}