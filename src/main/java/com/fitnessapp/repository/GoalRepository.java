package com.fitnessapp.repository;

import com.fitnessapp.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Integer> {

    // Добавете този метод, ако не съществува
    Optional<Goal> findByName(String name);
    // Добавете този метод, ако не съществува (за case-insensitive търсене)
    Optional<Goal> findByNameIgnoreCase(String name);
}