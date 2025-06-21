package com.fitnessapp.model;

public enum DayOfWeek {
    MONDAY("Понеделник"),
    TUESDAY("Вторник"),
    WEDNESDAY("Сряда"),
    THURSDAY("Четвъртък"),
    FRIDAY("Петък"),
    SATURDAY("Събота"),
    SUNDAY("Неделя");

    private final String displayName;

    DayOfWeek(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}