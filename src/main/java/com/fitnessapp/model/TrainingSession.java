package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

// Премахнете или променете този импорт:
// import java.time.DayOfWeek;
// Трябва да използваме DayOfWeek от текущия пакет (com.fitnessapp.model)

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "training_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TrainingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Включва 'id' в equals() и hashCode()
    private Integer id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_plan_id", nullable = false)
    private TrainingPlan trainingPlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // Уверете се, че използвате DayOfWeek от com.fitnessapp.model пакета
    private DayOfWeek dayOfWeek; // Това вече ще реферира към com.fitnessapp.model.DayOfWeek

    private Integer durationMinutes;


    @OneToMany(mappedBy = "trainingSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Exercise> exercises = new ArrayList<>();


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