package com.fitnessapp.service;

import com.fitnessapp.model.*;
import com.fitnessapp.repository.MealRepository;
import com.fitnessapp.repository.UserRepository;
import com.fitnessapp.repository.DietTypeRepository;
import com.fitnessapp.repository.ActivityLevelRepository;
import com.fitnessapp.repository.GoalRepository;
import com.fitnessapp.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private final NutritionPlanService nutritionPlanService;
    private final TrainingPlanService trainingPlanService;
    private final UserRepository userRepository;
    private final MealRepository mealRepository;
    private final DietTypeRepository dietTypeRepository;
    private final ActivityLevelRepository activityLevelRepository;
    private final GoalRepository goalRepository;
    private final RoleRepository roleRepository;

    private final Map<String, SessionState> sessionMap = new HashMap<>();

    @Autowired
    public ChatbotService(NutritionPlanService nutritionPlanService,
                          TrainingPlanService trainingPlanService,
                          UserRepository userRepository,
                          MealRepository mealRepository,
                          DietTypeRepository dietTypeRepository,
                          ActivityLevelRepository activityLevelRepository,
                          GoalRepository goalRepository,
                          RoleRepository roleRepository) {
        this.nutritionPlanService = nutritionPlanService;
        this.trainingPlanService = trainingPlanService;
        this.userRepository = userRepository;
        this.mealRepository = mealRepository;
        this.dietTypeRepository = dietTypeRepository;
        this.activityLevelRepository = activityLevelRepository;
        this.goalRepository = goalRepository;
        this.roleRepository = roleRepository;
    }

    // Клас за състоянието на сесията
    public static class SessionState {
        String state = "ASK_DIET_EXPLANATION"; // Начално състояние
        String email;
        String fullName;
        String dietType;
        Double weight;
        Double height;
        Integer age;
        String gender;
        String goal;
        String activityLevel;
        String meatPreference;
        Boolean consumesDairy;
        String trainingType;
        Set<String> allergies = new HashSet<>();
        Set<String> otherDietaryPreferences = new HashSet<>();
        Integer trainingDaysPerWeek;
        Integer trainingDurationMinutes;
        String level;
        String mealFrequencyPreference;

        public Integer userId;
        public boolean isGuest = true;
        public boolean planGenerated = false;
    }


    public SessionState getOrCreateSession(String sessionId) {
        return sessionMap.computeIfAbsent(sessionId, k -> new SessionState());
    }


    private void resetSession(String sessionId) {
        sessionMap.put(sessionId, new SessionState());
    }

    public String processMessage(String sessionId, String message) {
        SessionState session = sessionMap.computeIfAbsent(sessionId, k -> new SessionState());

        if (message.equalsIgnoreCase("рестарт")) {
            resetSession(sessionId);
            return "Здравейте! Аз съм вашият личен асистент за фитнес и хранене. Готови ли сте да създадем вашия персонализиран план? Първо, искате ли да научите повече за различните типове диети? (да / не)";
        }

        String response;
        try {
            response = switch (session.state) {
                case "ASK_DIET_EXPLANATION" -> handleDietExplanation(session, message);
                case "ASK_DIET_TYPE" -> handleDietTypeInput(session, message);
                case "ASK_WEIGHT" -> handleWeightInput(session, message);
                case "ASK_HEIGHT" -> handleHeightInput(session, message);
                case "ASK_AGE" -> handleAgeInput(session, message);
                case "ASK_GENDER" -> handleGenderInput(session, message);
                case "ASK_GOAL" -> handleGoalInput(session, message);
                case "ASK_ACTIVITY_LEVEL" -> handleActivityLevelInput(session, message);
                case "ASK_MEAT_PREFERENCE" -> handleMeatPreference(session, message);
                case "ASK_DAIRY_PREFERENCE" -> handleDairy(session, message);
                case "ASK_TRAINING_TYPE" -> handleTrainingType(session, message);
                case "ASK_ALLERGIES" -> handleAllergies(session, message);
                case "ASK_OTHER_DIETARY_PREFERENCES" -> handleOtherDietaryPreferences(session, message);
                case "ASK_TRAINING_DAYS_PER_WEEK" -> handleTrainingDaysPerWeek(session, message);
                case "ASK_TRAINING_DURATION_MINUTES" -> handleTrainingDurationMinutes(session, message);
                case "ASK_LEVEL" -> handleLevel(session, message);
                case "ASK_MEAL_FREQUENCY" -> handleMealFrequency(session, message);
                case "ASK_EMAIL" -> handleEmail(session, message);
                case "ASK_FULL_NAME" -> handleFullName(session, message);
                case "DONE" -> "Вашият персонализиран режим е вече изчислен! Ако желаете да генерирате нов, моля, напишете 'рестарт'.";
                default -> "Изглежда има проблем с текущото състояние. Моля, напишете 'рестарт', за да започнем отначало.";
            };
        } catch (Exception e) {
            e.printStackTrace();
            return "Възникна вътрешна грешка при обработката на съобщението: " + e.getMessage() + ". Моля, опитайте отново или напишете 'рестарт'.";
        }

        return response;
    }


    public boolean isReadyToGeneratePlan(String sessionId) {
        SessionState session = sessionMap.get(sessionId);
        return session != null && "DONE".equals(session.state);
    }


    public NutritionPlan generatePlan(String sessionId) {
        SessionState session = sessionMap.get(sessionId);
        if (session == null || !"DONE".equals(session.state)) {
            throw new IllegalStateException("Сесията не е готова за генериране на план или не е намерена.");
        }

        User user = new User();
        user.setEmail(session.email);
        user.setFullName(session.fullName);


        user.setPassword("temporary_password_hashed");


        user.setDietType(dietTypeRepository.findByNameIgnoreCase(session.dietType)
                .orElseThrow(() -> new RuntimeException("DietType не е намерен: " + session.dietType)));
        user.setActivityLevel(activityLevelRepository.findByNameIgnoreCase(session.activityLevel)
                .orElseThrow(() -> new RuntimeException("ActivityLevel не е намерен: " + session.activityLevel)));
        user.setGoal(goalRepository.findByNameIgnoreCase(session.goal)
                .orElseThrow(() -> new RuntimeException("Goal не е намерен: " + session.goal)));


        user.setAge(session.age);
        user.setWeight(session.weight);
        user.setHeight(session.height);


        if (session.gender != null) {
            try {
                user.setGender(GenderType.fromString(session.gender));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Невалиден пол в сесията: " + session.gender + " - " + e.getMessage(), e);
            }
        }


        if (session.meatPreference != null) {
            try {
                user.setMeatPreference(MeatPreferenceType.fromString(session.meatPreference));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Невалидно предпочитание за месо в сесията: " + session.meatPreference + " - " + e.getMessage(), e);
            }
        }

        user.setConsumesDairy(session.consumesDairy);


        if (session.trainingType != null) {
            try {
                user.setTrainingType(TrainingType.fromString(session.trainingType));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Невалиден тип тренировка в сесията: " + session.trainingType + " - " + e.getMessage(), e);
            }
        }

        user.setAllergies(session.allergies);
        user.setOtherDietaryPreferences(session.otherDietaryPreferences);
        user.setTrainingDaysPerWeek(session.trainingDaysPerWeek);
        user.setTrainingDurationMinutes(session.trainingDurationMinutes);


        if (session.level != null) {
            try {
                user.setLevel(LevelType.fromString(session.level));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Невалидно фитнес ниво в сесията: " + session.level + " - " + e.getMessage(), e);
            }
        }


        if (session.mealFrequencyPreference != null) {
            try {
                String displayString;
                switch (session.mealFrequencyPreference) {
                    case "2": displayString = "2 пъти дневно"; break;
                    case "3": displayString = "3 пъти дневно"; break;
                    case "4": displayString = "4 пъти дневно"; break;
                    case "5": displayString = "5 пъти дневно"; break;
                    case "6": displayString = "6 пъти дневно"; break;
                    default:
                        throw new IllegalArgumentException("Невалидна честота на хранене в сесията: " + session.mealFrequencyPreference);
                }
                user.setMealFrequencyPreference(MealFrequencyPreferenceType.fromString(displayString));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Невалидно предпочитание за честота на хранене в сесията: " + session.mealFrequencyPreference + " - " + e.getMessage(), e);
            }
        }



        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER не е намерен!"));
        user.setRoles(Set.of(userRole));


        User savedUser = userRepository.save(user);

        // Генерираме и запазваме плана за хранене и връщаме NutritionPlan
        NutritionPlan nutritionPlan = nutritionPlanService.generateNutritionPlan(savedUser);

        // Генерираме и запазваме тренировъчния план
        trainingPlanService.generateAndSaveTrainingPlanForUser(savedUser);

        System.out.println("План генериран за потребител: " + savedUser.getFullName());
        return nutritionPlan; // Връщаме генерирания план за хранене
    }

    private String handleDietExplanation(SessionState session, String message) {
        if (message.trim().equalsIgnoreCase("да")) {
            session.state = "ASK_DIET_TYPE";
            return """
            📊 Разбира се, ето кратко резюме на основните диетични подходи:

            🍏 **Балансирана диета:** Фокусира се върху здравословна, разнообразна храна с умерено съотношение на макронутриенти (протеини, въглехидрати, мазнини). Идеална за поддържане на общо здраве и енергийни нива.
            🥩 **Протеинова диета:** Богата на протеини (месо, риба, яйца, млечни продукти) и често по-ниска на въглехидрати. Подходяща за натрупване на мускулна маса, възстановяване и чувство за ситост.
            🥑 **Кетогенна диета:** Силно ограничава въглехидратите, замествайки ги с мазнини. Тялото влиза в състояние на кетоза, което спомага за изгаряне на мазнини и може да е ефективна за отслабване и контрол на кръвната захар.
            🌱 **Веган диета:** Изключва всички животински продукти, включително месо, млечни продукти, яйца и мед. Базира се изцяло на растителни храни, богати на фибри, витамини и минерали.
            🍲 **Вегетарианска диета:** Подобна на веган, но позволява млечни продукти и яйца.
            🌰 **Палео диета:** Фокусира се върху храни, достъпни за палеолитните хора (месо, риба, зеленчуци, плодове, ядки, семена), изключва зърнени култури, бобови растения, млечни продукти и преработени храни.

            Моля, изберете диета, която най-добре отговаря на вашите нужди: **балансирана / протеинова / кето / веган / вегетарианска / палео**
            """;
        } else if (message.trim().equalsIgnoreCase("не")) {
            session.state = "ASK_DIET_TYPE";
            return "Разбрано. Моля, изберете предпочитан тип диета: **балансирана / протеинова / кето / веган / вегетарианска / палео**";
        } else {
            return "Моля, отговорете с 'да' или 'не', за да продължим. Искате ли информация за диетите?";
        }
    }

    private String handleDietTypeInput(SessionState session, String message) {
        String input = message.trim().toLowerCase();
        String dietName;

        switch (input) {
            case "балансирана":
                dietName = "Standard";
                break;
            case "протеинова":
                dietName = "High-Protein";
                break;
            case "кето":
                dietName = "Keto";
                break;
            case "веган":
                dietName = "Vegan";
                break;
            case "вегетарианска":
                dietName = "Vegetarian";
                break;
            case "палео":
                dietName = "Paleo";
                break;
            default:
                return "Не разбрах избора Ви. Моля, изберете един от предложените типове диети: **балансирана / протеинова / кето / веган / вегетарианска / палео**";
        }

        Optional<DietType> dietTypeOptional = dietTypeRepository.findByNameIgnoreCase(dietName);
        if (dietTypeOptional.isPresent()) {
            session.dietType = dietTypeOptional.get().getName();
            session.state = "ASK_WEIGHT";
            return "Моля, въведете вашето текущо тегло **в килограми (кг)**.";
        }
        return "Не разбрах избора Ви. Моля, изберете един от предложените типове диети: **балансирана / протеинова / кето / веган / вегетарианска / палео**";
    }

    private String handleWeightInput(SessionState session, String message) {
        try {
            double weight = Double.parseDouble(message);
            if (weight < 30 || weight > 250) {
                return "Въведеното тегло изглежда нереалистично. Моля, въведете тегло в диапазона от 30 до 250 кг.";
            }
            session.weight = weight;
            session.state = "ASK_HEIGHT";
            return "Благодаря! Сега, моля, въведете вашия ръст **в сантиметри (см)**.";
        } catch (NumberFormatException e) {
            return "Невалиден формат за тегло. Моля, въведете само число. Например: 75.5";
        }
    }

    private String handleHeightInput(SessionState session, String message) {
        try {
            double height = Double.parseDouble(message);
            if (height < 100 || height > 250) {
                return "Въведеният ръст изглежда нереалистичен. Моля, въведете ръст в диапазона от 100 до 250 см.";
            }
            session.height = height;
            session.state = "ASK_AGE";
            return "Чудесно! Моля, въведете вашата възраст **в години**.";
        } catch (NumberFormatException e) {
            return "Невалиден формат за ръст. Моля, въведете само число. Например: 178";
        }
    }

    private String handleAgeInput(SessionState session, String message) {
        try {
            int age = Integer.parseInt(message);
            if (age < 10 || age > 100) {
                return "Моля, въведете реалистична възраст между 10 и 100 години.";
            }
            session.age = age;
            session.state = "ASK_GENDER";
            return "Благодаря! Моля, посочете вашия пол: **мъж / жена**";
        } catch (NumberFormatException e) {
            return "Невалиден формат за възраст. Моля, въведете цяло число. Например: 30";
        }
    }

    private String handleGenderInput(SessionState session, String message) {
        String input = message.trim().toLowerCase();
        if (input.equals("мъж") || input.equals("жена")) {
            session.gender = input;
            session.state = "ASK_GOAL";
            return "Разбрано. Каква е вашата основна фитнес цел? **отслабване / мускулна маса / поддържане**";
        } else {
            return "Невалиден отговор. Моля, посочете пол: **мъж / жена**";
        }
    }

    private String handleGoalInput(SessionState session, String message) {
        String input = message.trim().toLowerCase();
        String goalName;

        switch (input) {
            case "отслабване":
                goalName = "Weight Loss";
                break;
            case "мускулна маса":
                goalName = "Muscle Gain";
                break;
            case "поддържане":
                goalName = "Maintain";
                break;
            default:
                return "Не разбрах целта Ви. Моля, изберете една от предложените: **отслабване / мускулна маса / поддържане**";
        }

        Optional<Goal> goalOptional = goalRepository.findByNameIgnoreCase(goalName);
        if (goalOptional.isPresent()) {
            session.goal = goalOptional.get().getName();
            session.state = "ASK_ACTIVITY_LEVEL";
            return "Моля, опишете вашето ниво на физическа активност: **малко / леко / умерено / активно / много активно**";
        }
        return "Не разбрах целта Ви. Моля, изберете една от предложените: **отслабване / мускулна маса / поддържане**";
    }

    private String handleActivityLevelInput(SessionState session, String message) {
        String input = message.trim().toLowerCase();
        String activityLevelName;

        switch (input) {
            case "малко":
                activityLevelName = "Sedentary";
                break;
            case "леко":
                activityLevelName = "Lightly Active";
                break;
            case "умерено":
                activityLevelName = "Moderately Active";
                break;
            case "активно":
                activityLevelName = "Very Active";
                break;
            case "много активно":
                activityLevelName = "Extra Active";
                break;
            default:
                return "Не разбрах нивото на активност. Моля, изберете: **малко / леко / умерено / активно / много активно**";
        }

        Optional<ActivityLevel> activityLevelOptional = activityLevelRepository.findByNameIgnoreCase(activityLevelName);
        if (activityLevelOptional.isPresent()) {
            session.activityLevel = activityLevelOptional.get().getName();
            session.state = "ASK_MEAT_PREFERENCE";
            return "Имате ли предпочитания относно консумацията на месо? **пилешко / телешко / риба / свинско / агнешко / без месо / няма значение**";
        }
        return "Не разбрах нивото на активност. Моля, изберете: **малко / леко / умерено / активно / много активно**";
    }

    private String handleMeatPreference(SessionState session, String message) {
        String input = message.trim().toLowerCase();
        List<String> validPreferences = Arrays.asList("пилешко", "телешко", "риба", "свинско", "агнешко", "без месо", "няма значение");

        if (validPreferences.contains(input)) {
            //  Съхраняваме оригиналния входен стринг, който `fromString` методът може да обработи.
            session.meatPreference = input;
            session.state = "ASK_DAIRY_PREFERENCE";
            return "Консумирате ли млечни продукти? (да / не)";
        } else {
            return "Невалиден отговор. Моля, изберете: **пилешко / телешко / риба / свинско / агнешко / без месо / няма значение**";
        }
    }

    private String handleDairy(SessionState session, String message) {
        String input = message.trim().toLowerCase();
        if (input.equals("да")) {
            session.consumesDairy = true;
            session.state = "ASK_TRAINING_TYPE";
            return "Какъв тип тренировки предпочитате? **тежести / без тежести / кардио**";
        } else if (input.equals("не")) {
            session.consumesDairy = false;
            session.state = "ASK_TRAINING_TYPE";
            return "Какъв тип тренировки предпочитате? **тежести / без тежести / кардио**";
        } else {
            return "Моля, отговорете с 'да' или 'не'.";
        }
    }

    private String handleTrainingType(SessionState session, String message) {
        String input = message.trim().toLowerCase();
        List<String> validTypes = Arrays.asList("тежести", "без тежести", "кардио");
        if (validTypes.contains(input)) {
            session.trainingType = input;
            session.state = "ASK_ALLERGIES";
            return "Имате ли някакви хранителни алергии? Моля, избройте ги, разделени със запетая (напр. 'ядки, глутен, яйца') или напишете 'не'.";
        } else {
            return "Невалиден тип тренировка. Моля, изберете: **тежести / без тежести / кардио**";
        }
    }

    private String handleAllergies(SessionState session, String message) {
        String input = message.trim();
        if (input.equalsIgnoreCase("не") || input.isEmpty()) {
            session.allergies = Collections.emptySet();
        } else {
            session.allergies = Arrays.stream(input.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        }
        session.state = "ASK_OTHER_DIETARY_PREFERENCES";
        return "Имате ли други специални хранителни предпочитания или ограничения (напр. 'без захар', 'нискомаслено', 'без соя')? Моля, избройте ги, разделени със запетая, или напишете 'не'.";
    }

    private String handleOtherDietaryPreferences(SessionState session, String message) {
        String input = message.trim();
        if (input.equalsIgnoreCase("не") || input.isEmpty()) {
            session.otherDietaryPreferences = Collections.emptySet();
        } else {
            session.otherDietaryPreferences = Arrays.stream(input.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        }
        session.state = "ASK_TRAINING_DAYS_PER_WEEK";
        return "Колко дни в седмицата планирате да тренирате? (число от 1 до 7)";
    }

    private String handleTrainingDaysPerWeek(SessionState session, String message) {
        try {
            int days = Integer.parseInt(message);
            if (days >= 1 && days <= 7) {
                session.trainingDaysPerWeek = days;
                session.state = "ASK_TRAINING_DURATION_MINUTES";
                return "Колко минути обикновено трае една ваша тренировка? (число)";
            } else {
                return "Невалиден брой дни. Моля, въведете число от 1 до 7.";
            }
        } catch (NumberFormatException e) {
            return "Невалиден формат. Моля, въведете цяло число.";
        }
    }

    private String handleTrainingDurationMinutes(SessionState session, String message) {
        try {
            int duration = Integer.parseInt(message);
            if (duration >= 15 && duration <= 180) { // Разумни граници за тренировка
                session.trainingDurationMinutes = duration;
                session.state = "ASK_LEVEL"; // Следващо състояние за ниво на тренировка
                return "Какво е вашето текущо фитнес ниво? **начинаещ / средно напреднал / напреднал**";
            } else {
                return "Невалидна продължителност. Моля, въведете число между 15 и 180 минути.";
            }
        } catch (NumberFormatException e) {
            return "Невалиден формат. Моля, въведете цяло число.";
        }
    }

    private String handleLevel(SessionState session, String message) {
        String input = message.trim().toLowerCase();
        List<String> validLevels = Arrays.asList("начинаещ", "средно напреднал", "напреднал");
        if (validLevels.contains(input)) {
            session.level = input;
            session.state = "ASK_MEAL_FREQUENCY";
            return "Колко хранения на ден предпочитате да имате? (напр. '2', '3', '4', '5' или '6')";
        } else {
            return "Невалидно ниво. Моля, изберете: **начинаещ / средно напреднал / напреднал**";
        }
    }

    private String handleMealFrequency(SessionState session, String message) {
        String input = message.trim();
        try {
            int frequency = Integer.parseInt(input);
            if (frequency >= 2 && frequency <= 6) { // Разумен диапазон
                // Съхраняваме числото, то ще бъде преобразувано в display value в generatePlan
                session.mealFrequencyPreference = input;
                session.state = "ASK_EMAIL"; // Преминаваме към събиране на потребителски данни за запис
                return "Почти сме готови! Моля, въведете вашия имейл адрес, за да запазим плана ви.";
            } else {
                return "Невалидна честота на хранене. Моля, въведете число между 2 и 6.";
            }
        } catch (NumberFormatException e) {
            return "Невалиден формат. Моля, въведете число.";
        }
    }

    private String handleEmail(SessionState session, String message) {
        String email = message.trim();
        if (email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            session.email = email;
            session.state = "ASK_FULL_NAME";
            return "Благодаря! Сега, моля, въведете вашето пълно име (напр. Иван Петров).";
        } else {
            return "Невалиден имейл адрес. Моля, въведете валиден имейл.";
        }
    }

    private String handleFullName(SessionState session, String message) {
        String fullName = message.trim();
        if (!fullName.isEmpty() && fullName.length() > 3) { // Проста валидация
            session.fullName = fullName;
            session.state = "DONE"; // Всички данни са събрани, планът може да бъде генериран
            return "Всички необходими данни са събрани! Генерирам вашия персонализиран план...";
        } else {
            return "Моля, въведете пълното си име.";
        }
    }
}