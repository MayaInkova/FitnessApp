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
@CrossOrigin(origins = "http://localhost:5173")
public class ChatbotController {

    private final ChatbotService chatbotService;
    private static final Logger log = LoggerFactory.getLogger(ChatbotController.class);

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/message")
    public ResponseEntity<?> handleMessage(@RequestBody ChatMessageRequest req) {

        try {

            String sessionId = String.valueOf(req.getSessionId());
            log.info("📨  [{}] {}", sessionId, req.getMessage());


            ChatbotService.SessionState s = chatbotService.getOrCreateSession(sessionId);

            if (s.userId == null && req.getUserId() != null) {   // вече логнат
                s.userId  = req.getUserId();
                s.isGuest = false;
            }
            if (req.getUserId() == null) {                       // още е гост
                s.isGuest = true;
            }


            String reply = chatbotService.processMessage(sessionId, req.getMessage());


            if (!s.planGenerated && chatbotService.isReadyToGeneratePlan(sessionId)) {
                s.planGenerated = true;          // за да не влизаме пак тук


                if (s.isGuest) {
                    Map<String, Object> demoPlan = chatbotService.generateDemoPlan(sessionId); // <-- нов метод
                    return ResponseEntity.ok(Map.of(
                            "type", "demo_plan",
                            "plan", demoPlan
                    ));
                }


                NutritionPlan plan = chatbotService.generatePlan(sessionId);
                return ResponseEntity.ok(Map.of(
                        "type", "plan",
                        "plan", plan
                ));
            }


            return ResponseEntity.ok(Map.of(
                    "type",    "text",
                    "message", reply
            ));

        } catch (Exception ex) {
            log.error(" Грешка при обработка на съобщението", ex);
            return ResponseEntity.status(500).body(
                    "Грешка при обработка на съобщението: " + ex.getMessage()
            );
        }
    }


    @GetMapping("/status/{sessionId}")
    public ResponseEntity<?> getSessionStatus(@PathVariable String sessionId) {
        try {
            ChatbotService.SessionState s = chatbotService.getOrCreateSession(sessionId);
            return ResponseEntity.ok(Map.of(
                    "sessionId",     sessionId,
                    "isGuest",       s.isGuest,
                    "userId",        s.userId,
                    "planGenerated", s.planGenerated,
                    "state",         s.state          // за дебъг
            ));
        } catch (Exception ex) {
            log.error(" Грешка при проверка на състоянието", ex);
            return ResponseEntity.status(500).body("Грешка: " + ex.getMessage());
        }
    }
}