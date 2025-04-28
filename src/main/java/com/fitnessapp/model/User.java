package com.fitnessapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private Integer age;

    private Double height; // в см

    private Double weight; // в кг

    private String gender;

    private String activityLevel; // например: Low, Medium, High

    private String goal; // например: "weight_loss", "muscle_gain"

    // Конструктори
    public User() {
    }

    public User(Long id, String fullName, String email, String password, Integer age, Double height, Double weight, String gender, String activityLevel, String goal) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
        this.activityLevel = activityLevel;
        this.goal = goal;
    }

    // ГЕТЪРИ
    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Integer getAge() {
        return age;
    }

    public Double getHeight() {
        return height;
    }

    public Double getWeight() {
        return weight;
    }

    public String getGender() {
        return gender;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public String getGoal() {
        return goal;
    }

    // СЕТЪРИ
    public void setId(Long id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }
}