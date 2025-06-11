package com.fitnessapp.controller;

import com.fitnessapp.dto.ChatMessageRequest;
import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.service.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatbotController {

    private final ChatbotService chatbotService;
    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/message")
    public ResponseEntity<?> handleMessage(@RequestBody ChatMessageRequest request) {
        try {
            String sessionId = String.valueOf(request.getSessionId()); // 🔁 преобразуване към String
            logger.info("Получено съобщение: {}", request.getMessage());

            ChatbotService.SessionState session = chatbotService.getOrCreateSession(sessionId);

            if (session.userId == null && request.getUserId() != null) {
                session.userId = request.getUserId();
                session.isGuest = false;
            }

            if (request.getUserId() == null) {
                session.isGuest = true;
            }

            String result = chatbotService.processMessage(sessionId, request.getMessage());

            if (!session.planGenerated && chatbotService.isReadyToGeneratePlan(sessionId)) {
                session.planGenerated = true;

                if (session.isGuest) {
                    return ResponseEntity.ok(Map.of("type", "demo_plan_redirect"));
                }

                NutritionPlan plan = chatbotService.generatePlan(sessionId);
                return ResponseEntity.ok(Map.of("type", "plan", "plan", plan));
            }

            return ResponseEntity.ok(Map.of("type", "text", "message", result));

        } catch (Exception e) {
            logger.error("Грешка при обработка на съобщението", e);
            return ResponseEntity.status(500).body("Грешка при обработка на съобщението: " + e.getMessage());
        }
    }

    @GetMapping("/status/{sessionId}")
    public ResponseEntity<?> getSessionStatus(@PathVariable String sessionId) {
        try {
            ChatbotService.SessionState session = chatbotService.getOrCreateSession(sessionId);
            return ResponseEntity.ok(Map.of(
                    "sessionId", sessionId,
                    "isGuest", session.isGuest,
                    "userId", session.userId,
                    "planGenerated", session.planGenerated
            ));
        } catch (Exception e) {
            logger.error("Грешка при проверка на състоянието на сесията", e);
            return ResponseEntity.status(500).body("Грешка: " + e.getMessage());
        }
    }
}