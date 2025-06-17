package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter; // Добавено
import lombok.Setter; // Добавено
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; // Добавено

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Добавено за Objects.hash, ако е необходимо

@Entity
@Table(name = "training_sessions")
@Getter // Генерира гетъри
@Setter // Генерира сетъри
@NoArgsConstructor
@AllArgsConstructor
@Builder
// --- ВАЖНА ПРОМЯНА: Контролираме EqualsAndHashCode ---
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Генерира equals/hashCode само за полета с @EqualsAndHashCode.Include
public class TrainingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Включва 'id' в equals() и hashCode()
    private Integer id;

    // ManyToOne връзката е LAZY, затова не я включваме в equals/hashCode
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_plan_id", nullable = false)
    private TrainingPlan trainingPlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    private Integer durationMinutes;

    // OneToMany колекцията е LAZY, затова НЕ Я ВКЛЮЧВАМЕ В equals/hashCode
    @OneToMany(mappedBy = "trainingSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Exercise> exercises = new ArrayList<>();

    // --- Помощни методи за двупосочни връзки ---
    public void addExercise(Exercise exercise) {
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