package com.fitnessapp.repository;

import com.fitnessapp.model.ActivityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ActivityLevelRepository extends JpaRepository<ActivityLevel, Integer> {

    // Добавете този метод, ако не съществува
    Optional<ActivityLevel> findByName(String name);
    // Добавете този метод, ако не съществува (за case-insensitive търсене)
    Optional<ActivityLevel> findByNameIgnoreCase(String name);
}