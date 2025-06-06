package com.fitnessapp.controller;


import com.fitnessapp.dto.ChatMessageRequest;
import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.service.ChatbotService;
import com.fitnessapp.service.ChatbotService.SessionState;
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
            logger.info("Получено съобщение: {}", request.getMessage());

            SessionState session = chatbotService.getOrCreateSession(request.getSessionId());

            if (session.userId == null && request.getUserId() != null) {
                session.userId = request.getUserId();
                session.isGuest = false;
            }

            if (request.getUserId() == null) {
                session.isGuest = true;
            }

            // Задаване на диета
            if (request.getDietType() != null) {
                session.dietType = request.getDietType();
            }

            String result = chatbotService.processMessage(request.getSessionId(), request.getMessage());

            if (!session.planGenerated && chatbotService.isReadyToGeneratePlan(request.getSessionId())) {
                NutritionPlan plan = chatbotService.generatePlan(request.getSessionId());
                session.planGenerated = true;

                return ResponseEntity.ok(Map.of(
                        "type", "plan",
                        "plan", plan
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "type", "text",
                    "message", result
            ));

        } catch (Exception e) {
            logger.error("Грешка при обработка на съобщението", e);
            return ResponseEntity.status(500).body("Грешка при обработка на съобщението: " + e.getMessage());
        }
    }
}
