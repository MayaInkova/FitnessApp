package com.fitnessapp.model; // или com.fitnessapp.model.enums, както си го направил

public enum ExerciseType {
    WEIGHTS, // Промених STRENGTH на WEIGHTS, за да съвпада с TrainingType.WEIGHTS
    BODYWEIGHT, // Добавих BODYWEIGHT
    CARDIO,
    // FLEXIBILITY, BALANCE, ENDURANCE, OTHER - може да ги запазиш, ако ги ползваш,
    // но `TrainingPlanService` в момента мапва само към WEIGHTS, BODYWEIGHT, CARDIO
}