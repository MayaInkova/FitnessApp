package com.fitnessapp.repository;

import com.fitnessapp.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Integer> {}