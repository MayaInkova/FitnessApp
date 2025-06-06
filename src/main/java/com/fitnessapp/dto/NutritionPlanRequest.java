package com.fitnessapp.dto;

public class NutritionPlanRequest {
    private Long userId;
    private int age;
    private double height;
    private double weight;
    private String goal;
    private String gender;
    private String dietType; // ➕ ново поле

    public NutritionPlanRequest() {}


    public Long getUserId() {
        return userId;
    }

    public int getAge() {
        return age;
    }

    public double getHeight() {
        return height;
    }

    public double getWeight() {
        return weight;
    }

    public String getGoal() {
        return goal;
    }

    public String getGender() {
        return gender;
    }

    public String getDietType() {
        return dietType;
    }


    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setDietType(String dietType) {
        this.dietType = dietType;
    }
}
