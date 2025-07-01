package com.fitnessapp.repository;

import com.fitnessapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    // List<User> findAll(); // Този метод е вграден в JpaRepository, не е задължително да го декларирате изрично, но не е грешка да е тук.
    boolean existsByEmail(String email);

    // Намиране на потребител по токен за възстановяване на парола
    Optional<User> findByResetPasswordToken(String resetPasswordToken);

    // 🌟 НОВ МЕТОД: За търсене по имейл ИЛИ пълно име (case-insensitive)
    List<User> findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(String email, String fullName);
}