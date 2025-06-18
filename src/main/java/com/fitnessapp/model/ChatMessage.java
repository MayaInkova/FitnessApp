package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter; // Added
import lombok.Setter; // Added
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; // Added

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String messageContent;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    private MessageSenderType sender;
}