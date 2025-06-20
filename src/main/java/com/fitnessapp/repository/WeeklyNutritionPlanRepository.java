package com.fitnessapp.repository;

import com.fitnessapp.model.WeeklyNutritionPlan;
import com.fitnessapp.model.User; // Уверете се, че имате импорт за User
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyNutritionPlanRepository extends JpaRepository<WeeklyNutritionPlan, Integer> {
    // Метод за намиране на всички седмични планове за даден потребител, сортирани по начална дата низходящо
    List<WeeklyNutritionPlan> findByUserOrderByStartDateDesc(User user);

    // Метод за намиране на седмичен план за конкретен потребител и начална дата (ако е необходимо)
    Optional<WeeklyNutritionPlan> findByUserAndStartDate(User user, LocalDate startDate);
}