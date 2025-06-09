package com.fitnessapp.repository;

import com.fitnessapp.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Integer> {
    List<ChatSession> findByUserId(Integer userId);
}