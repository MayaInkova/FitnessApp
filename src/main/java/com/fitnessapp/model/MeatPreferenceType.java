package com.fitnessapp.model; // или com.fitnessapp.model.enums - избери едно

public enum MeatPreferenceType {
    NONE("без месо"), // За вегетарианци/вегани
    CHICKEN("пилешко"),
    BEEF("телешко"),
    PORK("свинско"),
    FISH("риба"),
    LAMB("агнешко"), // Добавих агнешко, ако го ползваш
    NO_PREFERENCE("няма значение");

    private final String displayName;

    MeatPreferenceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MeatPreferenceType fromString(String text) {
        for (MeatPreferenceType b : MeatPreferenceType.values()) {
            // Сравняваме display name (напр. "без месо") или name() (напр. "NONE")
            if (b.displayName.equalsIgnoreCase(text) || b.name().equalsIgnoreCase(text.replace(" ", "_").toUpperCase())) {
                return b;
            }
        }
        throw new IllegalArgumentException("Невалидна стойност за MeatPreferenceType: " + text);
    }
}