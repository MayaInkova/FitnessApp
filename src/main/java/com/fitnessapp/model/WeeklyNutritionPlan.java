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

    // Един седмичен план съдържа много дневни планове (обикновено 7)
    // Използваме "mappedBy" към полето weeklyNutritionPlan в NutritionPlan entity
    // CascadeType.ALL означава, че операции като persist, merge, remove, refresh ще се каскадират към свързаните NutritionPlan-и.
    // orphanRemoval = true означава, че ако дневен план бъде премахнат от колекцията dailyPlans, той ще бъде изтрит от базата данни.
    @OneToMany(mappedBy = "weeklyNutritionPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<NutritionPlan> dailyPlans = new ArrayList<>();

    // Можете да добавите snapshot полета или агрегирани данни тук, ако е необходимо,
    // подобно на NutritionPlan. Например, общи калории/макроси за седмицата.
    // Засега ще разчитаме на dailyPlans за тази информация.

    // Метод за добавяне на дневен план към седмичния
    // Важно: Този метод трябва да се използва, за да се поддържа двупосочна връзка
    public void addDailyPlan(NutritionPlan dailyPlan) {
        if (dailyPlans == null) {
            dailyPlans = new ArrayList<>();
        }
        dailyPlans.add(dailyPlan);
        dailyPlan.setWeeklyNutritionPlan(this); // Важно: свързваме и обратната връзка
    }

    // Метод за премахване на дневен план от седмичния
    public void removeDailyPlan(NutritionPlan dailyPlan) {
        if (dailyPlans != null) {
            dailyPlans.remove(dailyPlan);
            dailyPlan.setWeeklyNutritionPlan(null); // Премахваме връзката
        }
    }
}