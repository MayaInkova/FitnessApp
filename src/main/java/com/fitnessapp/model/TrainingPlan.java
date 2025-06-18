package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "training_plans", uniqueConstraints = {

        @UniqueConstraint(columnNames = {"user_id", "date_generated"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TrainingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @EqualsAndHashCode.Include
    private User user;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    private LocalDate dateGenerated;

    private Integer daysPerWeek;
    private Integer durationMinutes;

    @OneToMany(mappedBy = "trainingPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TrainingSession> trainingSessions = new ArrayList<>();


    public void addTrainingSession(TrainingSession session) {
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