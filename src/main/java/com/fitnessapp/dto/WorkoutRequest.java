package com.fitnessapp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class WorkoutRequest {
    private String goalName;
    private boolean hasWeights;
}