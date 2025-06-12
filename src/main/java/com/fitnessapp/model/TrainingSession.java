package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "training_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_plan_id", nullable = false)
    private TrainingPlan trainingPlan;

    @Enumerated(EnumType.STRING) // Използваме ENUM за DayOfWeek
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    private Integer durationMinutes;

    @OneToMany(mappedBy = "trainingSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Lombok ще инициализира това при изграждане
    private List<Exercise> exercises = new ArrayList<>();

    public void addExercise(Exercise exercise) {
        // Допълнителна защита, макар че @Builder.Default трябва да осигури инициализация
        if (this.exercises == null) {
            this.exercises = new ArrayList<>();
        }
        if (exercise != null) {
            exercises.add(exercise);
            exercise.setTrainingSession(this);
        }
    }

    public void removeExercise(Exercise exercise) {
        if (this.exercises != null && exercise != null) {
            exercises.remove(exercise);
            exercise.setTrainingSession(null);
        }
    }
}
