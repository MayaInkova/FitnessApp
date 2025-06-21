package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "weekly_nutrition_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyNutritionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "weeklyNutritionPlan",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<NutritionPlan> dailyPlans = new ArrayList<>();


    public void addDailyPlan(NutritionPlan dailyPlan) {
        if (dailyPlans == null) {
            dailyPlans = new ArrayList<>();
        }
        dailyPlans.add(dailyPlan);
        dailyPlan.setWeeklyNutritionPlan(this); // двупосочна връзка!
    }


    public void removeDailyPlan(NutritionPlan dailyPlan) {
        if (dailyPlans != null) {
            dailyPlans.remove(dailyPlan);
            dailyPlan.setWeeklyNutritionPlan(null);
        }
    }
}