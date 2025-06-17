package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter; // Добавено
import lombok.Setter; // Добавено
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; // Добавено

@Entity
@Table(name = "food_items")
@Getter // Генерира гетъри
@Setter // Генерира сетъри
@NoArgsConstructor
@AllArgsConstructor
@Builder
// --- ВАЖНА ПРОМЯНА: Контролираме EqualsAndHashCode за последователност ---
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Генерира equals/hashCode само за полета с @EqualsAndHashCode.Include
public class FoodItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Включва 'id' в equals() и hashCode()
    private Integer id;

    @Column(nullable = false)
    @EqualsAndHashCode.Include // Можете да включите и 'name', ако смятате, че е достатъчно уникално за equals/hashCode
    private String name;

    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private String unit;
    private Double quantity;
}