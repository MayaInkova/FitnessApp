package com.fitnessapp.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String messageText;

    @Enumerated(EnumType.STRING)
    private Sender sender; // USER or BOT

    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "chat_session_id")
    private ChatSession chatSession;

    public enum Sender {
        USER,
        BOT
    }
}