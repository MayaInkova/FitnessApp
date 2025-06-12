package com.fitnessapp.repository;

import com.fitnessapp.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Integer> {


    Optional<Goal> findByName(String name);
    Optional<Goal> findByNameIgnoreCase(String name);
}