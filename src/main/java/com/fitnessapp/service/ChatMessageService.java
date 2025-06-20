package com.fitnessapp.service;

import com.fitnessapp.model.ChatMessage;
import com.fitnessapp.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatMessage saveMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getBySessionId(Integer sessionId) {
        return chatMessageRepository.findBySession_Id(sessionId);
    }
}