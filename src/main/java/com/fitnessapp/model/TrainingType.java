package com.fitnessapp.model; // или com.fitnessapp.model.enums

public enum TrainingType {
    WEIGHTS("тежести"),
    BODYWEIGHT("без тежести"),
    CARDIO("кардио");

    private final String displayName;

    TrainingType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TrainingType fromString(String text) {
        for (TrainingType b : TrainingType.values()) {
            if (b.displayName.equalsIgnoreCase(text) || b.name().equalsIgnoreCase(text.replace(" ", "_").toUpperCase())) {
                return b;
            }
        }
        throw new IllegalArgumentException("Невалидна стойност за TrainingType: " + text);
    }
}