package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter; // Добавено
import lombok.Setter; // Добавено
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; // Добавено

@Entity
@Table(name = "goals")
@Getter // Генерира гетъри
@Setter // Генерира сетъри
@NoArgsConstructor
@AllArgsConstructor
@Builder
// --- ВАЖНА ПРОМЯНА: Контролираме EqualsAndHashCode за последователност ---
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Генерира equals/hashCode само за полета с @EqualsAndHashCode.Include
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Включва 'id' в equals() и hashCode()
    private Integer id;

    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include // Можете да включите и 'name', тъй като е уникално
    private String name;

    private String description;

    private Double calorieModifier;
}