package com.fitnessapp.model; // или com.fitnessapp.model.enums

public enum GenderType {
    MALE("мъж"),
    FEMALE("жена");

    private final String displayName;

    GenderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static GenderType fromString(String text) {
        for (GenderType b : GenderType.values()) {
            if (b.displayName.equalsIgnoreCase(text) || b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Невалидна стойност за GenderType: " + text);
    }
}