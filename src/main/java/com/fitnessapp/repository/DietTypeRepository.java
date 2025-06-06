package com.fitnessapp.repository;

import com.fitnessapp.model.DietType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DietTypeRepository extends JpaRepository<DietType, Integer> {
    Optional<DietType> findByNameIgnoreCase(String name);
}