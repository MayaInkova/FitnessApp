package com.fitnessapp.controller;

import com.fitnessapp.service.ChatSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatSessionService chatSessionService;

    public ChatController(ChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    @PostMapping("/message")
    public ResponseEntity<String> receiveMessage(@RequestParam String sessionId, @RequestBody String userInput) {
        chatSessionService.processUserMessage(sessionId, userInput);
        return ResponseEntity.ok("Message received and processed");
    }
}