package com.fitnessapp.repository;

import com.fitnessapp.model.ChatMessage;
import com.fitnessapp.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatSessionOrderByTimestampAsc(ChatSession chatSession);
}