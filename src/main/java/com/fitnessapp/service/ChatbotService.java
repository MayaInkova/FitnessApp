package com.fitnessapp.service;

import com.fitnessapp.model.*;
import com.fitnessapp.repository.MealRepository;
import com.fitnessapp.repository.UserRepository;
import com.fitnessapp.repository.DietTypeRepository;
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
    private final DietTypeRepository dietTypeRepository;

    private final Map<String, SessionState> sessionMap = new HashMap<>();

    @Autowired
    public ChatbotService(NutritionPlanService nutritionPlanService,
                          UserRepository userRepository,
                          MealRepository mealRepository,
                          DietTypeRepository dietTypeRepository) {
        this.nutritionPlanService = nutritionPlanService;
        this.userRepository = userRepository;
        this.mealRepository = mealRepository;
        this.dietTypeRepository = dietTypeRepository;
    }

    public String processMessage(String sessionId, String message) {
        SessionState session = sessionMap.computeIfAbsent(sessionId, k -> new SessionState());

        if (message.equalsIgnoreCase("рестарт")) {
            resetSession(sessionId);
            return "Сесията е рестартирана. Искаш ли малко информация за диетите преди да избереш? (да / не)";
        }

        // обработка при първоначално състояние
        if ("ASK_DIET_EXPLANATION".equals(session.state)) {
            if (message.trim().equalsIgnoreCase("да")) {
                session.state = "ASK_DIET_TYPE";
                return """
                Ето кратка информация:
                🟢 Balanced – разнообразна храна, умерени количества.
                🥩 High Protein – за мускулна маса, повече месо и протеин.
                🥑 Keto – високо съдържание на мазнини, ниско на въглехидрати.

                Сега избери тип диета: balanced / high_protein / keto
            """;
            } else if (message.trim().equalsIgnoreCase("не")) {
                session.state = "ASK_DIET_TYPE";
                return "Добре. Моля, избери вид диета: balanced / high_protein / keto";
            } else {
                return "Искаш ли кратко обяснение за видовете диети? Отговори с 'да' или 'не'.";
            }
        }

        if ("ASK_DIET_TYPE".equals(session.state)) {
            if (message.equalsIgnoreCase("keto") || message.equalsIgnoreCase("balanced") || message.equalsIgnoreCase("high_protein")) {
                session.dietType = message.toLowerCase();
                session.state = "ASK_WEIGHT";
                return "Регистрирах тип диета: " + session.dietType + ". Колко тежиш в момента?";
            }
            return "Моля, избери тип диета: balanced / high_protein / keto";
        }

        return switch (session.state) {
            case "ASK_WEIGHT" -> handleWeightInput(session, message);
            case "ASK_HEIGHT" -> handleHeightInput(session, message);
            case "ASK_AGE" -> handleAgeInput(session, message);
            case "ASK_GENDER" -> handleGenderInput(session, message);
            case "ASK_GOAL" -> handleGoalInput(session, message);
            case "ASK_MEAT" -> handleMeatPreference(session, message);
            case "ASK_DAIRY" -> handleDairy(session, message);
            case "ASK_TRAINING" -> handleTraining(session, message);
            case "ASK_ALLERGIES" -> handleAllergies(session, message);
            case "DONE" -> "Вече ти изчислих режим! Ако искаш нов, напиши: рестарт";
            default -> "Нещо се обърка. Опитай отново.";
        };
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
        if (session == null) throw new IllegalStateException("Липсва активна сесия.");

        String dietTypeName = session.dietType != null ? session.dietType : "balanced";
        DietType dietTypeEntity = dietTypeRepository.findByNameIgnoreCase(dietTypeName)
                .orElseGet(() -> dietTypeRepository.findByNameIgnoreCase("balanced").orElse(null));

        User user = createOrUpdateUser(session, sessionId);
        user.setDietType(dietTypeEntity);
        userRepository.save(user);

        NutritionPlan plan = nutritionPlanService.generatePlanForUser(user, dietTypeName);

        for (Meal meal : plan.getMeals()) {
            meal.setTime(getDefaultMealTime(meal.getType()));
            mealRepository.save(meal);
        }

        List<Meal> meals = mealRepository.findByNutritionPlanId(plan.getId());
        plan.setMeals(meals);

        return plan;
    }

    private User createOrUpdateUser(SessionState session, String sessionId) {
        if (session.isGuest) {
            String tempEmail = "guest_" + sessionId + "@guest.com";
            DietType dietType = dietTypeRepository.findByNameIgnoreCase(
                            session.dietType != null ? session.dietType : "balanced")
                    .orElseThrow(() -> new RuntimeException("Невалиден тип диета: " + session.dietType));

            User demoUser = User.builder()
                    .age(session.age > 0 ? session.age : 25)
                    .height(session.height > 0 ? session.height : 175)
                    .weight(session.weight > 0 ? session.weight : 70)
                    .gender(session.gender != null ? session.gender : "мъж")
                    .goal(session.goal != null ? session.goal : "maintain")
                    .activityLevel("moderate")
                    .email(tempEmail)
                    .fullName("Гост потребител")
                    .password("guest")
                    .meatPreference(session.meatPreference)
                    .consumesDairy(session.consumesDairy != null ? session.consumesDairy : true)
                    .allergies(session.allergies)
                    .trainingType(session.trainingType)
                    .dietType(dietType)
                    .build();

            return userRepository.save(demoUser);
        }

        User user = userRepository.findById(session.userId).orElseThrow();
        updateUserWithSessionData(user, session);

        DietType dietType = dietTypeRepository.findByNameIgnoreCase(
                        session.dietType != null ? session.dietType : "balanced")
                .orElse(null);
        user.setDietType(dietType);

        return userRepository.save(user);
    }

    private void updateUserWithSessionData(User user, SessionState session) {
        if (session.weight > 0) user.setWeight(session.weight);
        if (session.height > 0) user.setHeight(session.height);
        if (session.age > 0) user.setAge(session.age);
        if (session.gender != null) user.setGender(session.gender);
        if (session.goal != null) user.setGoal(session.goal);
        if (session.meatPreference != null) user.setMeatPreference(session.meatPreference);
        if (session.consumesDairy != null) user.setConsumesDairy(session.consumesDairy);
        if (session.allergies != null) user.setAllergies(session.allergies);
        if (session.trainingType != null) user.setTrainingType(session.trainingType);
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

    public static class SessionState {
        public double weight;
        public double height;
        public int age;
        public String gender;
        public String goal;
        public String state = "ASK_DIET_EXPLANATION";
        public Integer userId;
        public boolean planGenerated = false;
        public boolean isGuest = false;
        public String dietType;

        public String meatPreference;
        public Boolean consumesDairy;
        public String trainingType;
        public String allergies;
    }


    private String handleWeightInput(SessionState session, String message) {
        try {
            double weight = Double.parseDouble(message);
            if (weight < 30 || weight > 250) return "Моля, въведи реалистично тегло между 30 и 250 кг.";
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
            if (height < 100 || height > 250) return "Моля, въведи ръст между 100 и 250 см.";
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
            if (age < 10 || age > 100) return "Моля, въведи реалистична възраст между 10 и 100 години.";
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
        return switch (goal) {
            case "отслабване" -> {
                session.goal = "weight_loss";
                session.state = "ASK_MEAT";
                yield "Какъв тип месо предпочиташ? (пиле / телешко / свинско / риба / вегетарианец)";
            }
            case "качване" -> {
                session.goal = "muscle_gain";
                session.state = "ASK_MEAT";
                yield "Какъв тип месо предпочиташ? (пиле / телешко / свинско / риба / вегетарианец)";
            }
            case "поддържане" -> {
                session.goal = "maintain";
                session.state = "ASK_MEAT";
                yield "Какъв тип месо предпочиташ? (пиле / телешко / свинско / риба / вегетарианец)";
            }
            default -> "Моля, избери цел: отслабване / качване / поддържане";
        };
    }

    private String handleMeatPreference(SessionState session, String message) {
        session.meatPreference = message.trim();
        session.state = "ASK_DAIRY";
        return "Консумираш ли млечни продукти? (да / не)";
    }

    private String handleDairy(SessionState session, String message) {
        session.consumesDairy = message.trim().equalsIgnoreCase("да");
        session.state = "ASK_TRAINING";
        return "Предпочиташ ли план с тежести или без тежести?";
    }

    private String handleTraining(SessionState session, String message) {
        session.trainingType = message.trim().toLowerCase();
        session.state = "ASK_ALLERGIES";
        return "Ако имаш алергии, напиши кои са. Ако нямаш, напиши 'не'.";
    }

    private String handleAllergies(SessionState session, String message) {
        session.allergies = message.trim();
        session.state = "DONE";
        return "Благодаря! Изчислявам персонализиран режим...";
    }
}