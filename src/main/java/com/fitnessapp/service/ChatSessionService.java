package com.fitnessapp.service;

import com.fitnessapp.model.ChatSession;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.ChatSessionRepository;
import com.fitnessapp.repository.UserRepository; // Добавен импорт за UserRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;

    @Autowired
    public ChatSessionService(ChatSessionRepository chatSessionRepository, UserRepository userRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.userRepository = userRepository;
    }

    public ChatSession startSession(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Потребител не е намерен с ID: " + userId));

        ChatSession session = ChatSession.builder()
                .startedAt(LocalDateTime.now())
                .user(user)
                .build();
        return chatSessionRepository.save(session);
    }

    public List<ChatSession> getAllSessions() {
        return chatSessionRepository.findAll();
    }

    public List<ChatSession> getUserChatSessions(User user) {
        return chatSessionRepository.findByUser(user);
    }
}