package com.fitnessapp.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;

    // Конструктор
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getter за email
    public String getEmail() {
        return email;
    }

    // Getter за password
    public String getPassword() {
        return password;
    }

    // Setter за email
    public void setEmail(String email) {
        this.email = email;
    }

    // Setter за password
    public void setPassword(String password) {
        this.password = password;
    }
}