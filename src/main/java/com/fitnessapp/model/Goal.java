package com.fitnessapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private double calorieModifier;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCalorieModifier() {
        return calorieModifier;
    }

    public void setCalorieModifier(double calorieModifier) {
        this.calorieModifier = calorieModifier;
    }
}




