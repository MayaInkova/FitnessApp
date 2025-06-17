package com.fitnessapp.repository;

import com.fitnessapp.model.TrainingPlan;
import com.fitnessapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Integer> {

    Optional<TrainingPlan> findByUserAndDateGenerated(User user, LocalDate dateGenerated);

    List<TrainingPlan> findByUserOrderByDateGeneratedDesc(User user);
}