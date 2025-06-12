package com.fitnessapp.repository;

import com.fitnessapp.model.ActivityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ActivityLevelRepository extends JpaRepository<ActivityLevel, Integer> {


    Optional<ActivityLevel> findByName(String name);
    Optional<ActivityLevel> findByNameIgnoreCase(String name);
}