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
@Getter // Generates getters
@Setter // Generates setters
@NoArgsConstructor
@AllArgsConstructor
@Builder
// --- IMPORTANT CHANGE: Control EqualsAndHashCode ---
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Generates equals/hashCode only for fields with @EqualsAndHashCode.Include
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Include 'id' in equals() and hashCode()
    private Integer id;

    // LAZY ManyToOne relationship - DO NOT INCLUDE IN equals/hashCode
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