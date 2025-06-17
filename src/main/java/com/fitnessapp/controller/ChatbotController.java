package com.fitnessapp.controller;

import com.fitnessapp.dto.ChatMessageRequest;
import com.fitnessapp.dto.FullPlanDTO; // Добавено, ако искате да връщате директно FullPlanDTO
import com.fitnessapp.model.User;
import com.fitnessapp.repository.UserRepository;
import com.fitnessapp.service.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:5173")
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(ChatbotController.class);

    public ChatbotController(ChatbotService chatbotService, UserRepository userRepository) {
        this.chatbotService = chatbotService;
        this.userRepository = userRepository;
    }

    @PostMapping("/message")
    public ResponseEntity<?> handleMessage(@RequestBody ChatMessageRequest req, Principal principal) {
        try {
            String sessionId = String.valueOf(req.getSessionId());
            log.info("📨  [{}] {}", sessionId, req.getMessage());

            Integer userId = null;
            boolean isGuest = true;

            if (principal != null && principal.getName() != null) {
                User user = userRepository.findByEmail(principal.getName())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + principal.getName()));
                userId = user.getId();
                isGuest = false;
            }

            // Инициализираме или актуализираме сесията на чатбота с данните за потребителя
            chatbotService.setSessionUser(sessionId, userId, isGuest);

            // Обработваме съобщението
            // processMessage вече връща Map<String, Object> с типа ("text", "plan", "error", "demo_plan")
            // и съответните данни. Контролерът просто трябва да върне този Map.
            Object responseFromChatbotService = chatbotService.processMessage(sessionId, req.getMessage());

            // Връщаме директно отговора от ChatbotService
            return ResponseEntity.ok(responseFromChatbotService);

        } catch (Exception ex) {
            log.error("Грешка при обработка на съобщението за сесия: {}", req.getSessionId(), ex);
            return ResponseEntity.status(500).body(
                    Map.of("type", "error", "message", "Грешка при обработка на съобщението: " + ex.getMessage())
            );
        }
    }


    @GetMapping("/status/{sessionId}")
    public ResponseEntity<?> getSessionStatus(@PathVariable String sessionId) {
        try {
            ChatbotService.SessionState s = chatbotService.getOrCreateSession(sessionId);
            return ResponseEntity.ok(Map.of(
                    "sessionId", sessionId,
                    "isGuest", s.isGuest,
                    "userId", s.userId,
                    "planGenerated", s.planGenerated,
                    "state", s.state // за дебъг
            ));
        } catch (Exception ex) {
            log.error("Грешка при проверка на състоянието за сесия: {}", sessionId, ex);
            return ResponseEntity.status(500).body(
                    Map.of("type", "error", "message", "Грешка: " + ex.getMessage())
            );
        }
    }
}