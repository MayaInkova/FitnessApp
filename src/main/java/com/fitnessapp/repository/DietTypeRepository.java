package com.fitnessapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.fitnessapp.model.DietType;

public interface DietTypeRepository extends JpaRepository<DietType, Integer> {

    // Добавете този метод, ако не съществува
    Optional<DietType> findByName(String name);
    // Добавете този метод, ако не съществува (за case-insensitive търсене)
    Optional<DietType> findByNameIgnoreCase(String name);
}