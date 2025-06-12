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

    @Enumerated(EnumType.STRING) // ДОБАВЕНА АНОТАЦИЯ
    @Column(nullable = false)
    private DayOfWeek dayOfWeek; // ПРОМЕНЕНО ОТ String НА DayOfWeek

    private Integer durationMinutes;

    @OneToMany(mappedBy = "trainingSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Exercise> exercises = new ArrayList<>();


    public void addExercise(Exercise exercise) {
        if (exercise != null) {
            exercises.add(exercise);
            exercise.setTrainingSession(this);
        }
    }
}