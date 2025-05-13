
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
        switch (input.toLowerCase()) {
            case "weight_loss", "отслабване":
                return WEIGHT_LOSS;
            case "maintain", "поддържане":
                return MAINTAIN;
            case "muscle_gain", "качване на маса":
                return MUSCLE_GAIN;
            default:
                throw new IllegalArgumentException("Невалидна цел: " + input);
        }
    }
}
