package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter; // Добавено
import lombok.Setter; // Добавено
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; // Добавено

@Entity
@Table(name = "diet_types")
@Getter // Генерира гетъри
@Setter // Генерира сетъри
@NoArgsConstructor
@AllArgsConstructor
@Builder
// --- ВАЖНА ПРОМЯНА: Контролираме EqualsAndHashCode за последователност ---
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Генерира equals/hashCode само за полета с @EqualsAndHashCode.Include
public class DietType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Включва 'id' в equals() и hashCode()
    private Integer id;

    @Column(unique = true, nullable = false)
    @EqualsAndHashCode.Include // Можете да включите и 'name', тъй като е уникално
    private String name;

    private String description;
}