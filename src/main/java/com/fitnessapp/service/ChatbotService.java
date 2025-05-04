package com.fitnessapp.service;

import com.fitnessapp.model.ChatSessionData;
import com.fitnessapp.model.User;
import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.Recipe;
import com.fitnessapp.repository.ChatSessionDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChatbotService {

    @Autowired
    private ChatSessionDataRepository sessionDataRepository;

    @Autowired
    private NutritionPlanService nutritionPlanService;

    public String processMessage(String sessionId, String message) {
        //  Рестарт на сесията
        if (message.equalsIgnoreCase("рестарт")) {
            resetSession(sessionId);
            ChatSessionData newSession = new ChatSessionData();
            newSession.setSessionId(sessionId);
            newSession.setState("ASK_WEIGHT");
            sessionDataRepository.save(newSession);
            return " Добре, започваме отначало. Колко тежиш в килограми?";
        }

        //  Изтриване на съдържанието на сесията (но не и самата сесия)
        if (message.equalsIgnoreCase("изтрий")) {
            Optional<ChatSessionData> sessionOpt = sessionDataRepository.findBySessionId(sessionId);
            if (sessionOpt.isPresent()) {
                ChatSessionData session = sessionOpt.get();
                session.setWeight(null);
                session.setHeight(null);
                session.setGender(null);
                session.setGoal(null);
                session.setState("ASK_WEIGHT");
                sessionDataRepository.save(session);
                return "Изчистих въведените данни. Колко тежиш в момента?";
            } else {
                return " Няма активна сесия за този ID.";
            }
        }

        // Зареждане или създаване на нова сесия
        ChatSessionData session = sessionDataRepository
                .findBySessionId(sessionId)
                .orElseGet(() -> {
                    ChatSessionData newSession = new ChatSessionData();
                    newSession.setSessionId(sessionId);
                    newSession.setState("ASK_WEIGHT");
                    return sessionDataRepository.save(newSession);
                });

        String response;

        switch (session.getState()) {
            case "ASK_WEIGHT":
                try {
                    double weight = Double.parseDouble(message);
                    if (weight < 30 || weight > 250) {
                        response = "Моля, въведи реалистично тегло между 30 и 250 кг.";
                    } else {
                        session.setWeight(weight);
                        session.setState("ASK_HEIGHT");
                        response = "Колко е твоят ръст в сантиметри?";
                    }
                } catch (NumberFormatException e) {
                    response = " Моля, въведи теглото си като число. Например: 70";
                }
                break;

            case "ASK_HEIGHT":
                try {
                    double height = Double.parseDouble(message);
                    if (height < 100 || height > 250) {
                        response = " Моля, въведи ръст между 100 и 250 см.";
                    } else {
                        session.setHeight(height);
                        session.setState("ASK_GENDER");
                        response = "Какъв е твоят пол? (мъж / жена)";
                    }
                } catch (NumberFormatException e) {
                    response = " Моля, въведи ръста си като число. Например: 175";
                }
                break;

            case "ASK_GENDER":
                String gender = message.trim().toLowerCase();
                if (gender.equals("мъж") || gender.equals("жена")) {
                    session.setGender(gender);
                    session.setState("ASK_GOAL");
                    response = "Каква е твоята цел? (weight_loss / muscle_gain / maintain)";
                } else {
                    response = " Моля, въведи пол: 'мъж' или 'жена'.";
                }
                break;

            case "ASK_GOAL":
                String goal = message.trim().toLowerCase();
                if (goal.equals("weight_loss") || goal.equals("muscle_gain") || goal.equals("maintain")) {
                    session.setGoal(goal);
                    session.setState("DONE");

                    //  Създаване на временен потребител
                    User tempUser = new User();
                    tempUser.setWeight(session.getWeight());
                    tempUser.setHeight(session.getHeight());
                    tempUser.setGender(session.getGender());
                    tempUser.setGoal(session.getGoal());
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
                    response = " Моля, въведи целта си: weight_loss / muscle_gain / maintain";
                }
                break;

            case "DONE":
                response = "Вече ти изчислих режим! Ако искаш нов, напиши: рестарт";
                break;

            default:
                response = " Нещо се обърка. Опитай отново.";
        }

        sessionDataRepository.save(session);
        return response;
    }

    public void resetSession(String sessionId) {
        Optional<ChatSessionData> session = sessionDataRepository.findBySessionId(sessionId);
        session.ifPresent(sessionDataRepository::delete);
    }
}