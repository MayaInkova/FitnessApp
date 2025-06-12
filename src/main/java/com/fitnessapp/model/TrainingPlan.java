package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "training_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private LocalDate dateGenerated;

    private Integer daysPerWeek;
    private Integer durationMinutes;

    @OneToMany(mappedBy = "trainingPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Lombok ще инициализира това при изграждане
    private List<TrainingSession> trainingSessions = new ArrayList<>();

    public void addTrainingSession(TrainingSession session) {
        // Допълнителна защита, макар че @Builder.Default трябва да осигури инициализация
        if (this.trainingSessions == null) {
            this.trainingSessions = new ArrayList<>();
        }
        if (session != null) {
            trainingSessions.add(session);
            session.setTrainingPlan(this);
        }
    }

    public void removeTrainingSession(TrainingSession session) {
        if (this.trainingSessions != null && session != null) {
            trainingSessions.remove(session);
            session.setTrainingPlan(null);
        }
    }
}
