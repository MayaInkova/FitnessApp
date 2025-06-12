package com.fitnessapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.fitnessapp.model.DietType;

public interface DietTypeRepository extends JpaRepository<DietType, Integer> {


    Optional<DietType> findByName(String name);
    Optional<DietType> findByNameIgnoreCase(String name);
}