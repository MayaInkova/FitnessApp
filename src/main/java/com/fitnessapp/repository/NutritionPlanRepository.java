package com.fitnessapp.repository;

import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionPlanRepository extends JpaRepository<NutritionPlan, Integer> { // Променено от Long на Integer
    Optional<NutritionPlan> findByUserAndDateGenerated(User user, LocalDate dateGenerated);
    List<NutritionPlan> findByUser(User user);

}