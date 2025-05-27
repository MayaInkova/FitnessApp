package com.fitnessapp.service;

import com.fitnessapp.model.Meal;
import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.Recipe;
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
                response = handleWeightInput(session, message);
                break;
            case "ASK_HEIGHT":
                response = handleHeightInput(session, message);
                break;
            case "ASK_AGE":
                response = handleAgeInput(session, message);
                break;
            case "ASK_GENDER":
                response = handleGenderInput(session, message);
                break;
            case "ASK_GOAL":
                response = handleGoalInput(session, message);
                break;
            case "DONE":
                response = "Вече ти изчислих режим! Ако искаш нов, напиши: рестарт";
                break;
            default:
                response = "Нещо се обърка. Опитай отново.";
        }

        return response;
    }

    public boolean isReadyToGeneratePlan(String sessionId) {
        SessionState session = sessionMap.get(sessionId);
        return session != null &&
                "DONE".equals(session.state) &&
                session.goal != null &&
                session.gender != null;
    }

    public NutritionPlan generatePlan(String sessionId) {
        SessionState session = sessionMap.get(sessionId);

        //  Гост потребител
        if (session.isGuest) {
            String tempEmail = "guest_" + sessionId + "@guest.com";

            User demoUser = User.builder()
                    .age(session.age > 0 ? session.age : 25)
                    .height(session.height > 0 ? session.height : 175)
                    .weight(session.weight > 0 ? session.weight : 70)
                    .gender(session.gender != null ? session.gender : "мъж")
                    .goal(session.goal != null ? session.goal : "maintain")
                    .activityLevel("moderate")
                    .email(tempEmail)
                    .fullName("Гост потребител")
                    .password("guest") // dummy value
                    .build();

            demoUser = userRepository.save(demoUser);

            return nutritionPlanService.generatePlanForUser(demoUser);
        }

        User user = null;
        if (session.userId != null) {
            user = userRepository.findById(session.userId).orElse(null);
            if (user != null) {
                updateUserWithSessionData(user, session);
                userRepository.save(user);
            }
        }

        if (user == null) {
            String email = "temp_" + sessionId + "@temp.com";
            user = userRepository.findByEmail(email).orElseGet(() -> {
                User tempUser = User.builder()
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
                return userRepository.save(tempUser);
            });
        }

        NutritionPlan plan = nutritionPlanService.generatePlanForUser(user);

        for (Meal meal : plan.getMeals()) {
            meal.setTime(getDefaultMealTime(meal.getType()));
            mealRepository.save(meal);
        }

        List<Meal> meals = mealRepository.findByNutritionPlanId(plan.getId());
        plan.setMeals(meals);
        return plan;
    }

    private void updateUserWithSessionData(User user, SessionState session) {
        if (session.weight > 0) user.setWeight(session.weight);
        if (session.height > 0) user.setHeight(session.height);
        if (session.age > 0) user.setAge(session.age);
        if (session.gender != null) user.setGender(session.gender);
        if (session.goal != null) user.setGoal(session.goal);
        user.setActivityLevel("moderate");
    }

    private String getDefaultMealTime(String type) {
        if (type == null) return "08:00";
        return switch (type.toLowerCase()) {
            case "закуска", "breakfast" -> "08:00";
            case "обяд", "lunch" -> "13:00";
            case "вечеря", "dinner" -> "19:00";
            default -> "10:30";
        };
    }

    public void resetSession(String sessionId) {
        sessionMap.put(sessionId, new SessionState());
    }

    public void attachUserToSession(String sessionId, Integer userId) {
        SessionState session = getOrCreateSession(sessionId);
        session.userId = userId;
    }

    public SessionState getOrCreateSession(String sessionId) {
        return sessionMap.computeIfAbsent(sessionId, k -> new SessionState());
    }

    //  Сесия
    public static class SessionState {
        public double weight;
        public double height;
        public int age;
        public String gender;
        public String goal;
        public String state = "ASK_WEIGHT";
        public Integer userId;
        public boolean planGenerated = false;
        public boolean isGuest = false;
    }

    //  ВЪТРЕШНИ МЕТОДИ
    private String handleWeightInput(SessionState session, String message) {
        try {
            double weight = Double.parseDouble(message);
            if (weight < 30 || weight > 250) {
                return "Моля, въведи реалистично тегло между 30 и 250 кг.";
            }
            session.weight = weight;
            session.state = "ASK_HEIGHT";
            return "Колко е твоят ръст в сантиметри?";
        } catch (NumberFormatException e) {
            return "Моля, въведи теглото си като число. Например: 70";
        }
    }

    private String handleHeightInput(SessionState session, String message) {
        try {
            double height = Double.parseDouble(message);
            if (height < 100 || height > 250) {
                return "Моля, въведи ръст между 100 и 250 см.";
            }
            session.height = height;
            session.state = "ASK_AGE";
            return "Колко години си?";
        } catch (NumberFormatException e) {
            return "Моля, въведи ръста си като число. Например: 175";
        }
    }

    private String handleAgeInput(SessionState session, String message) {
        try {
            int age = Integer.parseInt(message);
            if (age < 10 || age > 100) {
                return "Моля, въведи реалистична възраст между 10 и 100 години.";
            }
            session.age = age;
            session.state = "ASK_GENDER";
            return "Какъв е твоят пол? (мъж / жена)";
        } catch (NumberFormatException e) {
            return "Моля, въведи възрастта си като цяло число. Например: 25";
        }
    }

    private String handleGenderInput(SessionState session, String message) {
        String gender = message.trim().toLowerCase();
        if (gender.equals("мъж") || gender.equals("жена")) {
            session.gender = gender;
            session.state = "ASK_GOAL";
            return "Каква е твоята цел? (отслабване / качване / поддържане)";
        }
        return "Моля, въведи пол: 'мъж' или 'жена'.";
    }

    private String handleGoalInput(SessionState session, String message) {
        String goal = message.trim().toLowerCase();
        switch (goal) {
            case "отслабване" -> session.goal = "weight_loss";
            case "качване" -> session.goal = "muscle_gain";
            case "поддържане" -> session.goal = "maintain";
            default -> {
                return "Моля, избери цел: отслабване / качване / поддържане";
            }
        }
        session.state = "DONE";
        return "Благодаря! Изчислявам твоя режим...";
    }
}