package com.fitnessapp.controller;

import com.fitnessapp.dto.ChatMessageRequest;
import com.fitnessapp.dto.RecipeDTO;
import com.fitnessapp.model.MealType;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.UserRepository;
import com.fitnessapp.service.ChatbotService;
import com.fitnessapp.service.RecipeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:5173")
public class ChatbotController {

    private static final Logger log = LoggerFactory.getLogger(ChatbotController.class);

    private final ChatbotService chatbotService;
    private final UserRepository userRepository;
    private final RecipeService recipeService;

    public ChatbotController(ChatbotService chatbotService, UserRepository userRepository, RecipeService recipeService) {
        this.chatbotService = chatbotService;
        this.userRepository = userRepository;
        this.recipeService = recipeService;
    }

    @PostMapping("/message")
    public ResponseEntity<?> handleMessage(@RequestBody ChatMessageRequest req, Principal principal) {
        try {
            String sessionId = String.valueOf(req.getSessionId());
            log.info("  [{}] {}", sessionId, req.getMessage());

            Integer userId = null;
            boolean isGuest = true;

            if (principal != null && principal.getName() != null) {
                User user = userRepository.findByEmail(principal.getName())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + principal.getName()));
                userId = user.getId();
                isGuest = false;
            }

            chatbotService.setSessionUser(sessionId, userId, isGuest);
            Object response = chatbotService.processMessage(sessionId, req.getMessage());
            return ResponseEntity.ok(response);

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
                    "state", s.state
            ));
        } catch (Exception ex) {
            log.error("Грешка при проверка на състоянието за сесия: {}", sessionId, ex);
            return ResponseEntity.status(500).body(
                    Map.of("type", "error", "message", "Грешка: " + ex.getMessage())
            );
        }
    }

    @GetMapping("/alternatives")
    public ResponseEntity<List<RecipeDTO>> getAlternatives(
            @RequestParam MealType mealType,
            @RequestParam Integer excludeId
    ) {
        try {
            List<RecipeDTO> alternatives = recipeService.findAlternatives(mealType, excludeId);
            return ResponseEntity.ok(alternatives);
        } catch (Exception e) {
            log.error("Грешка при извличане на алтернативи: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}