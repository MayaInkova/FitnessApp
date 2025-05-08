package com.fitnessapp.service;

import com.fitnessapp.model.ChatMessage;
import com.fitnessapp.model.ChatSession;
import com.fitnessapp.model.ChatSessionData;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.ChatMessageRepository;
import com.fitnessapp.repository.ChatSessionDataRepository;
import com.fitnessapp.repository.ChatSessionRepository;
import com.fitnessapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionDataRepository chatSessionDataRepository;
    private final UserRepository userRepository;

    public ChatSessionService(ChatSessionRepository chatSessionRepository,
                              ChatMessageRepository chatMessageRepository,
                              ChatSessionDataRepository chatSessionDataRepository,
                              UserRepository userRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatSessionDataRepository = chatSessionDataRepository;
        this.userRepository = userRepository;
    }

    public void processUserMessage(String sessionId, String userInput) {
        ChatSessionData sessionData = chatSessionDataRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session"));

        // 1. Записваме user input като ChatMessage
        ChatSession chatSession = chatSessionRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    ChatSession newSession = new ChatSession();
                    newSession.setSessionId(sessionId);
                    newSession.setUser(sessionData.getUser());
                    newSession.setStartedAt(LocalDateTime.now());
                    return chatSessionRepository.save(newSession);
                });

        ChatMessage message = new ChatMessage();
        message.setChatSession(chatSession);
        message.setSender("USER");
        message.setMessage(userInput);
        message.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(message);

        // 2. (по избор) обработка на входните данни и state машината
        sessionData.setState("NEXT_STEP"); // update състояние
        chatSessionDataRepository.save(sessionData);
    }
}
