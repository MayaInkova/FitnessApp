package com.fitnessapp.controller;

import com.fitnessapp.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    // Променяме метода, така че да получава цялото тяло на заявката
    @PostMapping("/message")
    public String handleMessage(@RequestBody ChatMessageRequest request) {
        if (request.getMessage().trim().equalsIgnoreCase("рестарт")) {
            chatbotService.resetSession(request.getSessionId());
            return "🔄 Сесията е рестартирана. Колко тежиш в момента?";
        }

        return chatbotService.processMessage(request.getSessionId(), request.getMessage());
    }

    // Клас за обект на заявка, който съдържа sessionId и message
    public static class ChatMessageRequest {
        private String sessionId;
        private String message;

        // Гетъри и сетъри
        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}