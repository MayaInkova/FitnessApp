package com.fitnessapp.repository;

import com.fitnessapp.model.ChatSessionData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatSessionDataRepository extends JpaRepository<ChatSessionData, Long> {
    Optional<ChatSessionData> findBySessionId(String sessionId);
}