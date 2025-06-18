package com.fitnessapp.controller;

import com.fitnessapp.model.ChatSession;
import com.fitnessapp.model.User;
import com.fitnessapp.service.ChatSessionService;
import com.fitnessapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat-sessions")
@CrossOrigin(origins = "*")


public class ChatSessionController {

    private static final Logger logger = LoggerFactory.getLogger(ChatSessionController.class);

    private final ChatSessionService chatSessionService;
    private final UserService userService;

    @Autowired
    public ChatSessionController(ChatSessionService chatSessionService, UserService userService) {
        this.chatSessionService = chatSessionService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<ChatSession>> getAll() {
        logger.info("Получена GET заявка за всички чат сесии.");
        return ResponseEntity.ok(chatSessionService.getAllSessions());
    }

    @PostMapping("/start/{userId}")
    public ResponseEntity<ChatSession> startChatSession(@PathVariable Integer userId) {
        try {
            logger.info("Получена POST заявка за стартиране на чат сесия за потребител ID: {}", userId);
            // Извикваме startSession от service, който вече ще се грижи за намирането на User
            ChatSession newSession = chatSessionService.startSession(userId);
            logger.info("Нова чат сесия стартирана за потребител ID {}: Session ID {}", userId, newSession.getId());
            return ResponseEntity.ok(newSession);
        } catch (RuntimeException e) { // Хващаме RuntimeException, ако потребителят не е намерен
            logger.error("Грешка при стартиране на чат сесия за потребител ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(404).body(null); // Връщаме 404 Not Found
        } catch (Exception e) {
            logger.error("Възникна грешка при стартиране на чат сесия за потребител ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatSession>> getUserSessions(@PathVariable Integer userId) {
        try {
            logger.info("Получена GET заявка за чат сесии за потребител ID: {}", userId);
            User user = userService.getUserById(userId); // Намираме User обекта
            if (user == null) {
                logger.warn("Потребител с ID {} не е намерен при търсене на чат сесии.", userId);
                return ResponseEntity.notFound().build();
            }
            List<ChatSession> sessions = chatSessionService.getUserChatSessions(user); // Извикваме метода
            logger.info("Намерени {} чат сесии за потребител ID: {}", sessions.size(), userId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            logger.error("Възникна грешка при извличане на чат сесии за потребител ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}