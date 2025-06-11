package com.fitnessapp.model;

import java.util.Arrays;

public enum GoalType {
    WEIGHT_LOSS("отслабване", "Weight Loss"),
    MUSCLE_GAIN("мускулна маса", "Muscle Gain"),
    MAINTAIN("поддържане", "Maintain");

    private final String naturalLanguage;
    private final String displayName;

    GoalType(String naturalLanguage, String displayName) {
        this.naturalLanguage = naturalLanguage;
        this.displayName = displayName;
    }

    public String getNaturalLanguage() {
        return naturalLanguage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static GoalType fromString(String text) {
        for (GoalType b : GoalType.values()) {
            if (b.naturalLanguage.equalsIgnoreCase(text) || b.displayName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}