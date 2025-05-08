package com.fitnessapp.service;

import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.Recipe;
import com.fitnessapp.model.User;
import com.fitnessapp.service.NutritionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatbotService {

    private final NutritionPlanService nutritionPlanService;

    // Сесии, съхранявани в паметта
    private final Map<String, SessionState> sessionMap = new HashMap<>();

    @Autowired
    public ChatbotService(NutritionPlanService nutritionPlanService) {
        this.nutritionPlanService = nutritionPlanService;
    }

    public String processMessage(String sessionId, String message) {
        // Получаваме или създаваме нова сесия
        SessionState session = sessionMap.getOrDefault(sessionId, new SessionState());
        String response;

        if (message.equalsIgnoreCase("рестарт")) {
            resetSession(sessionId);  // използваме метода по-долу
            return "Сесията е рестартирана. Колко тежиш в момента?";
        }

        switch (session.state) {
            case "ASK_WEIGHT":
                try {
                    double weight = Double.parseDouble(message);
                    if (weight < 30 || weight > 250) {
                        response = "Моля, въведи реалистично тегло между 30 и 250 кг.";
                    } else {
                        session.weight = weight;
                        session.state = "ASK_HEIGHT";
                        response = "Колко е твоят ръст в сантиметри?";
                    }
                } catch (NumberFormatException e) {
                    response = "Моля, въведи теглото си като число. Например: 70";
                }
                break;

            case "ASK_HEIGHT":
                try {
                    double height = Double.parseDouble(message);
                    if (height < 100 || height > 250) {
                        response = "Моля, въведи ръст между 100 и 250 см.";
                    } else {
                        session.height = height;
                        session.state = "ASK_GENDER";
                        response = "Какъв е твоят пол? (мъж / жена)";
                    }
                } catch (NumberFormatException e) {
                    response = "Моля, въведи ръста си като число. Например: 175";
                }
                break;

            case "ASK_GENDER":
                String gender = message.trim().toLowerCase();
                if (gender.equals("мъж") || gender.equals("жена")) {
                    session.gender = gender;
                    session.state = "ASK_GOAL";
                    response = "Каква е твоята цел? (weight_loss / muscle_gain / maintain)";
                } else {
                    response = "Моля, въведи пол: 'мъж' или 'жена'.";
                }
                break;

            case "ASK_GOAL":
                String goal = message.trim().toLowerCase();
                if (goal.equals("weight_loss") || goal.equals("muscle_gain") || goal.equals("maintain")) {
                    session.goal = goal;
                    session.state = "DONE";

                    User tempUser = new User();
                    tempUser.setWeight(session.weight);
                    tempUser.setHeight(session.height);
                    tempUser.setGender(session.gender);
                    tempUser.setGoal(session.goal);
                    tempUser.setActivityLevel("moderate");

                    NutritionPlan plan = nutritionPlanService.generatePlanForUser(tempUser);

                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("Препоръчителен дневен прием: %.0f ккал.\n", plan.getCalories()));
                    sb.append("Примерни рецепти:\n");
                    for (Recipe recipe : plan.getRecipes()) {
                        sb.append("- ").append(recipe.getName()).append("\n");
                    }
                    response = sb.toString();
                } else {
                    response = "Моля, въведи целта си: weight_loss / muscle_gain / maintain";
                }
                break;

            case "DONE":
                response = "Вече ти изчислих режим! Ако искаш нов, напиши: рестарт";
                break;

            default:
                response = "Нещо се обърка. Опитай отново.";
        }

        // Запазваме състоянието
        sessionMap.put(sessionId, session);
        return response;
    }

    //  Метод за рестартиране на сесията
    public void resetSession(String sessionId) {
        sessionMap.put(sessionId, new SessionState());
    }

    //  Клас за състоянието на сесията
    private static class SessionState {
        double weight;
        double height;
        String gender;
        String goal;
        String state = "ASK_WEIGHT";
    }
}