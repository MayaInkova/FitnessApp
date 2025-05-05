package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatSessionData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String sessionId;

    private Double weight;
    private Double height;
    private String gender;
    private String goal;
    private String state = "ASK_WEIGHT";

    @ManyToOne
    @JoinColumn(name = "user_id")  // тук ще се пази връзката към потребителя
    private User user;
}