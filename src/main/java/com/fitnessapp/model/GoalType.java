package com.fitnessapp.model;

public enum GoalType {
    WEIGHT_LOSS("Отслабване"),
    MAINTAIN("Поддържане"),
    MUSCLE_GAIN("Качване на маса");

    private final String displayName;

    GoalType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static GoalType fromString(String input) {
        return switch (input.toLowerCase()) {
            case "weight_loss" -> WEIGHT_LOSS;
            case "maintain" -> MAINTAIN;
            case "muscle_gain" -> MUSCLE_GAIN;
            default -> throw new IllegalArgumentException("Invalid goal: " + input);
        };
    }
}
