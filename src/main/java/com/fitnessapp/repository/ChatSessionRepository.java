package com.fitnessapp.repository;

import com.fitnessapp.model.ChatSession;
import com.fitnessapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Integer> {
    List<ChatSession> findByUser(User user);

}