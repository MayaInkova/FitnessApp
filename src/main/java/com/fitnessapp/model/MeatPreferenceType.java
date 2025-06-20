package com.fitnessapp.model;

public enum MeatPreferenceType {
    NONE("без месо"),
    VEGETARIAN("вегетарианско"),
    CHICKEN("пилешко"),
    BEEF("телешко"),
    PORK("свинско"),
    NO_PORK("без свинско"), // Добавено
    FISH("риба"),
    NO_FISH("без риба"),   // Добавено
    LAMB("агнешко"),
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
            if (b.displayName.equalsIgnoreCase(text) || b.name().equalsIgnoreCase(text.replace(" ", "_").toUpperCase())) {
                return b;
            }
        }
        throw new IllegalArgumentException("Невалидна стойност за MeatPreferenceType: " + text);
    }
}