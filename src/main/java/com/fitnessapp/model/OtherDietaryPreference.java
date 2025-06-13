package com.fitnessapp.model;

public enum OtherDietaryPreference {
    GLUTEN_FREE,    // Без глутен
    NUT_FREE,       // Без ядки
    FISH_FREE,      // Без риба (ако не е покрито от MeatPreference.NO_FISH)
    SOY_FREE,       // Без соя
    DAIRY_FREE,     // Без млечни продукти (макар че ConsumesDairy е по-конкретно)
    VEGAN,          // Веган (може да се припокрива с DietType)
    PALEO,          // Палео диета
    KETOGENIC,      // Кетогенна диета
    LOW_CARB,       // Нисковъглехидратна
    HIGH_PROTEIN,   // Високопротеинова
    // Добавете други предпочитания, ако е необходимо
}