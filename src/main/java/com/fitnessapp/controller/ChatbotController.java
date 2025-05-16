package com.fitnessapp.controller;

import com.fitnessapp.service.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fitnessapp.dto.ChatMessageRequest;

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
            // Получаваме текстовия отговор от бота
            String result = chatbotService.processMessage(request.getSessionId(), request.getMessage());

            // Ако сме готови да генерираме хранителен режим (потребителят е въвел всичко)
            if (chatbotService.isReadyToGeneratePlan(request.getSessionId())) {
                // Генерираме обект с хранителен план
                var plan = chatbotService.generatePlan(request.getSessionId());

                // Връщаме JSON структурата към front-end
                return ResponseEntity.ok(Map.of(
                        "type", "plan",
                        "plan", plan
                ));
            }

            // Ако още събираме данни – връщаме текстов отговор
            return ResponseEntity.ok(Map.of(
                    "type", "text",
                    "message", result
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Грешка при обработка на съобщението: " + e.getMessage());
        }
    }
}