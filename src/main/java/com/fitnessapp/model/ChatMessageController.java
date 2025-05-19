package com.fitnessapp.controller;

import com.fitnessapp.model.ChatMessage;
import com.fitnessapp.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat-messages")
@CrossOrigin(origins = "*")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @Autowired
    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @PostMapping
    public ResponseEntity<ChatMessage> create(@RequestBody ChatMessage message) {
        return ResponseEntity.ok(chatMessageService.saveMessage(message));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getBySession(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(chatMessageService.getBySessionId(sessionId));
    }
}