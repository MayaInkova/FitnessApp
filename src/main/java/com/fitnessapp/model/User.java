package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private Integer age;
    private Double height;
    private Double weight;

    @Enumerated(EnumType.STRING) // Използваме enum за пол
    private GenderType gender;

    // Свързани с тренировъчен план
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_level_id")
    private ActivityLevel activityLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    @Enumerated(EnumType.STRING) // Използваме enum за тип тренировка
    private TrainingType trainingType;

    private Integer trainingDaysPerWeek; // Брой дни за тренировка
    private Integer trainingDurationMinutes; // Продължителност на една тренировка в минути

    @Enumerated(EnumType.STRING) // Използваме enum за ниво
    private LevelType level; // beginner, intermediate, advanced

    // Свързани с хранителен план
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_type_id")
    private DietType dietType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_allergies", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "allergy")
    @Builder.Default
    private Set<String> allergies = new HashSet<>();

    @Enumerated(EnumType.STRING) // Използваме enum за предпочитания за месо
    private MeatPreferenceType meatPreference;

    private Boolean consumesDairy;

    @Enumerated(EnumType.STRING) // Използваме enum за честота на хранене
    private MealFrequencyPreferenceType mealFrequencyPreference;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_other_dietary_preferences", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "preference")
    @Builder.Default
    private Set<String> otherDietaryPreferences = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // НОВИ: OneToOne връзки към TrainingPlan и NutritionPlan
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private TrainingPlan trainingPlan;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private NutritionPlan nutritionPlan;

    // Хелпер методи за добавяне на планове (опционално, но добра практика)
    public void setTrainingPlan(TrainingPlan trainingPlan) {
        if (trainingPlan != null) {
            trainingPlan.setUser(this);
        }
        this.trainingPlan = trainingPlan;
    }

    public void setNutritionPlan(NutritionPlan nutritionPlan) {
        if (nutritionPlan != null) {
            nutritionPlan.setUser(this);
        }
        this.nutritionPlan = nutritionPlan;
    }
}