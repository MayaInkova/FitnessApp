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
@Table(name = "chat_sessions")
@Getter // Generates getters
@Setter // Generates setters
@NoArgsConstructor
@AllArgsConstructor
@Builder
// --- IMPORTANT CHANGE: Control EqualsAndHashCode ---
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Generates equals/hashCode only for fields with @EqualsAndHashCode.Include
public class ChatSession {

    // LAZY ManyToOne relationship - DO NOT INCLUDE IN equals/hashCode
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Include 'id' in equals() and hashCode()
    private Integer id;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;
}