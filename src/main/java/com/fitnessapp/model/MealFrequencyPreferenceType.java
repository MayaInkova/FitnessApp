package com.fitnessapp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MealFrequencyPreferenceType {
    TWO_TIMES_DAILY("2 пъти дневно"),
    THREE_TIMES_DAILY("3 пъти дневно"),
    FOUR_TIMES_DAILY("4 пъти дневно"),
    FIVE_TIMES_DAILY("5 пъти дневно"),
    SIX_TIMES_DAILY("6 пъти дневно"),
    NO_PREFERENCE("няма значение");

    private final String displayValue;

    MealFrequencyPreferenceType(String displayValue) {
        this.displayValue = displayValue;
    }

    @JsonValue
    public String getDisplayValue() {
        return displayValue;
    }

    @JsonCreator
    public static MealFrequencyPreferenceType fromString(String text) {
        for (MealFrequencyPreferenceType b : MealFrequencyPreferenceType.values()) {
            if (b.displayValue.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Невалидна стойност за предпочитание за честота на хранене: " + text + ". Моля, изберете от '2 пъти дневно', '3 пъти дневно', '4 пъти дневно', '5 пъти дневно', '6 пъти дневно' или 'няма значение'.");
    }
}