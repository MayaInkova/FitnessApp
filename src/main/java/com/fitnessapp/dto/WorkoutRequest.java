package com.fitnessapp.dto;

import lombok.Data;

@Data
public class WorkoutRequest {
    private String goal;       // Пример: "muscle_gain", "weight_loss", "maintain"
    private boolean hasWeights;
}