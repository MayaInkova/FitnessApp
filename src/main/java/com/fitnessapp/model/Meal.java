package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; // Добавено

@Entity
@Table(name = "meals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// --- ВАЖНА ПРОМЯНА: Контролираме EqualsAndHashCode ---
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Генерира equals/hashCode само за полета с @EqualsAndHashCode.Include
public class Meal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Включва 'id' в equals() и hashCode()
    private Integer id;

    // LAZY ManyToOne relationship - DO NOT INCLUDE IN equals/hashCode
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutrition_plan_id")
    private NutritionPlan nutritionPlan;

    // LAZY ManyToOne relationship - DO NOT INCLUDE IN equals/hashCode
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Enumerated(EnumType.STRING)
    private MealType mealType;

    private Double portionSize;

    // --- Ръчните equals() и hashCode() методи са ПРЕМАХНАТИ,
    // --- за да може Lombok да генерира правилните!
}