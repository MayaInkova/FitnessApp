package com.fitnessapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*; // Увери се, че @Data или @Getter са тук

import java.util.ArrayList;
import java.util.List;


@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "users")
@Data // <-- Увери се, че тази анотация е тук! Тя генерира getters/setters.
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private Integer age;
    private Double height;
    private Double weight;
    private String gender;
    private String activityLevel;
    private String goal;
    private String meatPreference;
    private Boolean consumesDairy;
    private String trainingType;
    private String allergies;

    private String level; // Например: "beginner", "intermediate", "advanced"


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private List<Role> roles = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "diet_type_id")
    private DietType dietType;
}