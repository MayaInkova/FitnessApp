package com.fitnessapp.model; // или com.fitnessapp.model.enums

public enum LevelType {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    // преобразуване на стринг към LevelType
    public static LevelType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Текстовата стойност за LevelType не може да бъде празна.");
        }
        String normalizedText = text.trim().toLowerCase();
        return switch (normalizedText) {
            case "начинаещ" -> BEGINNER;
            case "средно напреднал" -> INTERMEDIATE;
            case "напреднал" -> ADVANCED;
            default -> throw new IllegalArgumentException("Невалидна стойност за фитнес ниво: " + text);
        };
    }
}