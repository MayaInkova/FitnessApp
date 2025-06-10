package com.fitnessapp.repository;

import com.fitnessapp.model.DietType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DietTypeRepository extends JpaRepository<DietType, Integer> {

    Optional<DietType> findByName(String name);

    Optional<DietType> findByNameIgnoreCase(String name);
}