package com.fitnessapp.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public class UserProfileUpdateDto {

    private String gender; // male, female

    @NotNull
    @Min(value = 1, message = "Age must be positive")
    private Integer age;

    @NotNull
    @Min(value = 1, message = "Height must be positive")
    private Integer height;

    @NotNull
    @Min(value = 1, message = "Weight must be positive")
    private Double weight;

    private String goal; // e.g., "weight_loss", "maintain", "muscle_gain"
    private String activityLevel; // e.g., "sedentary", "light", "moderate", "active", "very_active"

    private Long dietTypeId; // ID на типа диета (този тип е Long)
    private String allergies; // Няколко алергии, разделени със запетая
    private Boolean consumesDairy;
    private String meatPreference; // e.g., "none", "chicken", "fish", "red_meat"

    // Constructors
    public UserProfileUpdateDto() {
    }

    // Getters and Setters for all fields
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel; // <-- ПОПРАВЕНА ГРЕШКА ТУК
    }

    public Long getDietTypeId() {
        return dietTypeId;
    }

    public void setDietTypeId(Long dietTypeId) {
        this.dietTypeId = dietTypeId;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public Boolean getConsumesDairy() {
        return consumesDairy;
    }

    public void setConsumesDairy(Boolean consumesDairy) {
        this.consumesDairy = consumesDairy;
    }

    public String getMeatPreference() {
        return meatPreference;
    }

    public void setMeatPreference(String meatPreference) {
        this.meatPreference = meatPreference;
    }
}