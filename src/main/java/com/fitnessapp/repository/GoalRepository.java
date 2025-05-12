package com.fitnessapp.repository;

import com.fitnessapp.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Integer> {
    Optional<Goal> findByName(String name);
}