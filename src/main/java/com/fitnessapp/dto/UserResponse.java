package com.fitnessapp.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;
@Data
@Builder
public class UserResponse {
    private Integer id;
    private String fullName;
    private String email;
    private Set<String> roles; // Предполагаме, че това са имената на ролите (String)
}