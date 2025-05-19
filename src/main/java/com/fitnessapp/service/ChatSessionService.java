package com.fitnessapp.service;

import com.fitnessapp.model.ChatSession;
import com.fitnessapp.repository.ChatSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;

    @Autowired
    public ChatSessionService(ChatSessionRepository chatSessionRepository) {
        this.chatSessionRepository = chatSessionRepository;
    }

    public ChatSession startSession(Integer userId) {
        ChatSession session = ChatSession.builder()
                .startedAt(LocalDateTime.now())
                .build();
        return chatSessionRepository.save(session);
    }

    public List<ChatSession> getAllSessions() {
        return chatSessionRepository.findAll();
    }
}