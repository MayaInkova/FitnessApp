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
@Table(name = "nutrition_plans", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "dateGenerated"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NutritionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate dateGenerated;

    private Double targetCalories;

    @Column(name = "protein")
    private Double protein;

    @Column(name = "fat")
    private Double fat;

    @Column(name = "carbohydrates")
    private Double carbohydrates;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_gender_snapshot")
    private GenderType userGenderSnapshot;

    @Column(name = "user_age_snapshot")
    private Integer userAgeSnapshot;

    @Column(name = "user_weight_snapshot")
    private Double userWeightSnapshot;

    @Column(name = "user_height_snapshot")
    private Double userHeightSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_activity_level_snapshot_id")
    private ActivityLevel userActivityLevelSnapshot;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_diet_type_snapshot_id")
    private DietType userDietTypeSnapshot;

    @Column(name = "user_allergies_snapshot", length = 500)
    private String userAllergiesSnapshot;

    @Column(name = "user_other_dietary_preferences_snapshot", length = 500)
    private String userOtherDietaryPreferencesSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_meat_preference_snapshot")
    private MeatPreferenceType userMeatPreferenceSnapshot;

    @Column(name = "user_consumes_dairy_snapshot")
    private Boolean userConsumesDairySnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_meal_frequency_preference_snapshot")
    private MealFrequencyPreferenceType userMealFrequencyPreferenceSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    @OneToMany(mappedBy = "nutritionPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Meal> meals = new ArrayList<>();


    public void addMeal(Meal meal) {
        if (this.meals == null) {
            this.meals = new ArrayList<>();
        }
        this.meals.add(meal);
        meal.setNutritionPlan(this);
    }

    public void removeMeal(Meal meal) {
        if (this.meals != null) {
            this.meals.remove(meal);
            meal.setNutritionPlan(null);
        }
    }
}