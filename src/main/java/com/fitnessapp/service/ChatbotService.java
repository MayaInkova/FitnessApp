package com.fitnessapp.service;

import com.fitnessapp.model.Meal;
import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.MealRepository;
import com.fitnessapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    private final NutritionPlanService nutritionPlanService;
    private final UserRepository userRepository;
    private final MealRepository mealRepository;

    private final Map<String, SessionState> sessionMap = new HashMap<>();

    @Autowired
    public ChatbotService(NutritionPlanService nutritionPlanService,
                          UserRepository userRepository,
                          MealRepository mealRepository) {
        this.nutritionPlanService = nutritionPlanService;
        this.userRepository = userRepository;
        this.mealRepository = mealRepository;
    }

    public String processMessage(String sessionId, String message) {
        SessionState session = sessionMap.computeIfAbsent(sessionId, k -> new SessionState());
        String response;

        if (message.equalsIgnoreCase("рестарт")) {
            resetSession(sessionId);
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
                        session.state = "ASK_AGE";
                        response = "Колко години си?";
                    }
                } catch (NumberFormatException e) {
                    response = "Моля, въведи ръста си като число. Например: 175";
                }
                break;

            case "ASK_AGE":
                try {
                    int age = Integer.parseInt(message);
                    if (age < 10 || age > 100) {
                        response = "Моля, въведи реалистична възраст между 10 и 100 години.";
                    } else {
                        session.age = age;
                        session.state = "ASK_GENDER";
                        response = "Какъв е твоят пол? (мъж / жена)";
                    }
                } catch (NumberFormatException e) {
                    response = "Моля, въведи възрастта си като цяло число. Например: 25";
                }
                break;

            case "ASK_GENDER":
                String gender = message.trim().toLowerCase();
                if (gender.equals("мъж") || gender.equals("жена")) {
                    session.gender = gender;
                    session.state = "ASK_GOAL";
                    response = "Каква е твоята цел? (отслабване / качване / поддържане)";
                } else {
                    response = "Моля, въведи пол: 'мъж' или 'жена'.";
                }
                break;

            case "ASK_GOAL":
                String goalInput = message.trim().toLowerCase();
                switch (goalInput) {
                    case "отслабване":
                        session.goal = "weight_loss";
                        break;
                    case "качване":
                        session.goal = "muscle_gain";
                        break;
                    case "поддържане":
                        session.goal = "maintain";
                        break;
                    default:
                        return "Моля, избери цел: отслабване / качване / поддържане";
                }
                session.state = "DONE";
                response = "Благодаря! Изчислявам твоя режим...";
                break;

            case "DONE":
                response = "Вече ти изчислих режим! Ако искаш нов, напиши: рестарт";
                break;

            default:
                response = "Нещо се обърка. Опитай отново.";
        }

        sessionMap.put(sessionId, session);
        return response;
    }

    public boolean isReadyToGeneratePlan(String sessionId) {
        SessionState session = sessionMap.get(sessionId);
        return session != null
                && session.state.equals("DONE")
                && session.goal != null
                && session.gender != null;
    }

    public NutritionPlan generatePlan(String sessionId) {
        SessionState session = sessionMap.get(sessionId);
        String email = "temp_" + sessionId + "@temp.com";

        User tempUser = userRepository.findByEmail(email).orElse(null);

        if (tempUser == null) {
            tempUser = User.builder()
                    .email(email)
                    .fullName("Временен потребител")
                    .password("none")
                    .age(session.age)
                    .height(session.height)
                    .weight(session.weight)
                    .gender(session.gender)
                    .goal(session.goal)
                    .activityLevel("moderate")
                    .build();

            userRepository.save(tempUser);
        }

        NutritionPlan plan = nutritionPlanService.generatePlanForUser(tempUser);

        // ⚠️ Добавяме списъка с хранения (meals)
        List<Meal> meals = mealRepository.findByNutritionPlanId(plan.getId());
        plan.setMeals(meals);

        return plan;
    }

    public void resetSession(String sessionId) {
        sessionMap.put(sessionId, new SessionState());
    }

    private static class SessionState {
        double weight;
        double height;
        int age;
        String gender;
        String goal;
        String state = "ASK_WEIGHT";
    }
}
