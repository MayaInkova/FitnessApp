package com.fitnessapp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MealType {
    BREAKFAST("Закуска"),
    LUNCH("Обяд"),
    DINNER("Вечеря"),
    SNACK("Снак");

    private final String displayValue;

    MealType(String displayValue) {
        this.displayValue = displayValue;
    }

    @JsonValue
    public String getDisplayValue() {
        return displayValue;
    }

    @JsonCreator // Тази анотация е полезна за десериализация от JSON
    public static MealType fromString(String text) {
        for (MealType b : MealType.values()) {
            // Сравняваме с displayValue за по-гъвкаво съвпадениe
            if (b.displayValue.equalsIgnoreCase(text)) {
                return b;
            }
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Невалидна стойност за тип хранене: " + text + ". Очаква се 'Закуска', 'Обяд', 'Вечеря' или 'Снак'.");
    }
}