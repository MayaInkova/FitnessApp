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

    /**
     * Един седмичен план съдържа много дневни планове (обикновено 7).
     * - mappedBy: показва, че NutritionPlan има поле `weeklyNutritionPlan`, което е owner.
     * - cascade = ALL: при запазване/изтриване на Weekly → каскадно към NutritionPlan
     * - orphanRemoval = true: ако махнем дневен план от списъка → изтрива се от БД
     */
    @OneToMany(mappedBy = "weeklyNutritionPlan",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<NutritionPlan> dailyPlans = new ArrayList<>();

    /**
     * Добавяне на дневен план към седмичния.
     * ЗАДЪЛЖИТЕЛНО: трябва да зададе и обратната връзка, за да не се загуби при save().
     */
    public void addDailyPlan(NutritionPlan dailyPlan) {
        if (dailyPlans == null) {
            dailyPlans = new ArrayList<>();
        }
        dailyPlans.add(dailyPlan);
        dailyPlan.setWeeklyNutritionPlan(this); // двупосочна връзка!
    }

    /**
     * Премахване на дневен план.
     */
    public void removeDailyPlan(NutritionPlan dailyPlan) {
        if (dailyPlans != null) {
            dailyPlans.remove(dailyPlan);
            dailyPlan.setWeeklyNutritionPlan(null);
        }
    }
}