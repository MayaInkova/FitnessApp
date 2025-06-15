package com.fitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Добавен Builder за удобство
public class ChatMessageRequest {
    private String sessionId; // ChatSession entity има Integer ID
    private String message;
    private String dietTypeName;
    private String sender;
    public Integer userId;
}