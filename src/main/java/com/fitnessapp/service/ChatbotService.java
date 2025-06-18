package com.fitnessapp.service;

import com.fitnessapp.dto.FullPlanDTO;
import com.fitnessapp.dto.MealDTO;
import com.fitnessapp.dto.TrainingSessionDTO;
import com.fitnessapp.dto.ExerciseDTO; // Добавено
import com.fitnessapp.model.*;
import com.fitnessapp.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Добавено
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    private final NutritionPlanService nutritionPlanService;
    private final TrainingPlanService trainingPlanService;
    private final UserRepository userRepository;
    private final DietTypeRepository dietTypeRepository;
    private final ActivityLevelRepository activityLevelRepository;
    private final GoalRepository goalRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder; // Добавено за хеширане на пароли

    private final Map<String, SessionState> sessionMap = new HashMap<>();

    @Autowired
    public ChatbotService(NutritionPlanService nutritionPlanService,
                          TrainingPlanService trainingPlanService,
                          UserRepository userRepository,
                          DietTypeRepository dietTypeRepository,
                          ActivityLevelRepository activityLevelRepository,
                          GoalRepository goalRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) { // Добавен PasswordEncoder
        this.nutritionPlanService = nutritionPlanService;
        this.trainingPlanService = trainingPlanService;
        this.userRepository = userRepository;
        this.dietTypeRepository = dietTypeRepository;
        this.activityLevelRepository = activityLevelRepository;
        this.goalRepository = goalRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder; // Инжектиране на PasswordEncoder
    }

    public static class SessionState {

        public String state = "ASK_DIET_EXPLANATION";

        /* събрани данни */
        String email; // Може да се използва за гост акаунти
        String fullName; // Може да се използва за гост акаунти
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

        public Integer userId = null; // ID на потребителя, ако е логнат
        public boolean isGuest = true; // Флаг дали е гост или регистриран потребител
        public boolean planGenerated = false; // Показва дали планът е генериран за тази сесия
    }

    public SessionState getOrCreateSession(String sessionId) {
        return sessionMap.computeIfAbsent(sessionId, k -> new SessionState());
    }

    // Метод за задаване на userId и isGuest при старт на сесия
    public void setSessionUser(String sessionId, Integer userId, boolean isGuest) {
        SessionState session = getOrCreateSession(sessionId);
        session.userId = userId;
        session.isGuest = isGuest;
        logger.info("Сесия {} - userId: {}, isGuest: {}", sessionId, userId, isGuest);
    }

    // Метод за генериране на демо план (за GuestSummary.js)
    public Map<String, Object> generateDemoPlan(String sessionId) {
        SessionState s = sessionMap.get(sessionId);
        if (s == null) {

            s = new SessionState(); // Създаваме временна сесия за демото, ако няма налична
            s.dietType = "Балансирана"; // Задаваме базова диета за демото
            logger.warn("Сесия не е намерена за ID: {}. Генерирам базов демо план.", sessionId);
        } else {
            logger.info("Генерирам демо план за сесия: {} с диета: {}", sessionId, s.dietType);
        }

        List<Map<String, String>> meals = new ArrayList<>();

        // Примерни ястия, базирани на събраната диета
        meals.add(Map.of(
                "meal", "Закуска",
                "description", switch (Optional.ofNullable(s.dietType).orElse("Балансирана")) {
                    case "Кето" -> "Омлет от 3 яйца със спанак и авокадо";
                    case "Веган" -> "Овесени ядки с ядково мляко и боровинки";
                    case "Вегетарианска" -> "Кисело мляко с гранола и плод";
                    default -> "Овесени ядки с банан и мед"; // Балансирана
                }
        ));

        meals.add(Map.of(
                "meal", "Обяд",
                "description", switch (Optional.ofNullable(s.dietType).orElse("Балансирана")) {
                    case "Кето" -> "Салата със сьомга, маслини и зехтин";
                    case "Веган" -> "Будa боул с киноа, нахут и зеленчуци";
                    case "Вегетарианска" -> "Пълнозърнеста пита с яйчна салата";
                    default -> "Пилешко филе с кафяв ориз и броколи"; // Балансирана
                }
        ));

        meals.add(Map.of(
                "meal", "Вечеря",
                "description", switch (Optional.ofNullable(s.dietType).orElse("Балансирана")) {
                    case "Кето" -> "Телешки стек със зелен бейби спанак";
                    case "Веган" -> "Леща яхния с морков и целина";
                    case "Вегетарианска" -> "Фритата със зеленчуци и сирене";
                    default -> "Сьомга на фурна с аспержи"; // Балансирана
                }
        ));

        Map<String, Object> result = new HashMap<>();
        result.put("day", "Понеделник");
        result.put("meals", meals);

        // Добавяне на примерни тренировъчни сесии за демо план
        List<Map<String, String>> trainingSessions = new ArrayList<>();
        trainingSessions.add(Map.of(
                "name", "Тренировка за цяло тяло",
                "description", "Комплекс от основни упражнения за всички мускулни групи.",
                "duration", "45 минути",
                "exercises", "Клекове, лицеви опори, напади, планк, гребане с дъмбели."
        ));
        trainingSessions.add(Map.of(
                "name", "Кардио тренировка",
                "description", "Интервална тренировка с бягане или скачане на въже.",
                "duration", "30 минути",
                "exercises", "5 минути загрявка, 20 минути интервали (1 мин бързо, 1 мин бавно), 5 минути разтягане."
        ));
        result.put("trainingSessions", trainingSessions);

        return result;
    }

    private void resetSession(String sessionId) {
        sessionMap.put(sessionId, new SessionState());
        logger.info("Сесия {} е рестартирана.", sessionId);
    }

    // Променен тип на връщане на Object, за да може да връща Map<String, Object> или FullPlanDTO
    public Object processMessage(String sessionId, String message) {
        SessionState session = sessionMap.computeIfAbsent(sessionId, k -> new SessionState());

        if (message.equalsIgnoreCase("рестарт")) {
            resetSession(sessionId);
            return Map.of("type", "text", "message", "Здравейте! Аз съм вашият личен асистент за фитнес и хранене. Готови ли сте да създадем вашия персонализиран план? Първо, искате ли да научите повече за различните типове диети? (да / не)");
        }

        Object response;
        try {
            // Използваме state pattern за обработка на съобщения въз основа на текущото състояние
            response = switch (session.state) {
                case "ASK_DIET_EXPLANATION" -> Map.of("type", "text", "message", handleDietExplanation(session, message));
                case "ASK_DIET_TYPE" -> Map.of("type", "text", "message", handleDietTypeInput(session, message));
                case "ASK_WEIGHT" -> Map.of("type", "text", "message", handleWeightInput(session, message));
                case "ASK_HEIGHT" -> Map.of("type", "text", "message", handleHeightInput(session, message));
                case "ASK_AGE" -> Map.of("type", "text", "message", handleAgeInput(session, message));
                case "ASK_GENDER" -> Map.of("type", "text", "message", handleGenderInput(session, message));
                case "ASK_GOAL" -> Map.of("type", "text", "message", handleGoalInput(session, message));
                case "ASK_ACTIVITY_LEVEL" -> Map.of("type", "text", "message", handleActivityLevelInput(session, message));
                case "ASK_MEAT_PREFERENCE" -> Map.of("type", "text", "message", handleMeatPreference(session, message));
                case "ASK_DAIRY_PREFERENCE" -> Map.of("type", "text", "message", handleDairy(session, message));
                case "ASK_TRAINING_TYPE" -> Map.of("type", "text", "message", handleTrainingType(session, message));
                case "ASK_ALLERGIES" -> Map.of("type", "text", "message", handleAllergies(session, message));
                case "ASK_OTHER_DIETARY_PREFERENCES" -> Map.of("type", "text", "message", handleOtherDietaryPreferences(session, message));
                case "ASK_TRAINING_DAYS_PER_WEEK" -> Map.of("type", "text", "message", handleTrainingDaysPerWeek(session, message));
                case "ASK_TRAINING_DURATION_MINUTES" -> Map.of("type", "text", "message", handleTrainingDurationMinutes(session, message));
                case "ASK_LEVEL" -> Map.of("type", "text", "message", handleLevel(session, message));
                case "ASK_MEAL_FREQUENCY" -> handleMealFrequency(sessionId, session, message); // <-- Този метод вече връща Map
                case "DONE" ->
                        Map.of("type", "text", "message", "Вашият персонализиран режим е вече изчислен! Ако желаете да генерирате нов, моля, напишете 'рестарт'.");
                default ->
                        Map.of("type", "text", "message", "Изглежда има проблем с текущото състояние. Моля, напишете 'рестарт', за да започнем отначало.");
            };
        } catch (Exception e) {
            logger.error("Възникна вътрешна грешка при обработката на съобщението за сесия {}: {}", sessionId, e.getMessage(), e);
            return Map.of("type", "error", "message", "Възникна вътрешна грешка при обработката на съобщението: " + e.getMessage() + ". Моля, опитайте отново или напишете 'рестарт'.");
        }

        return response;
    }

    public boolean isReadyToGeneratePlan(String sessionId) {
        SessionState session = sessionMap.get(sessionId);
        return session != null && "DONE".equals(session.state);
    }

    // МОДИФИЦИРАН МЕТОД: generatePlan() -> generateAndSavePlanForUser() - вече връща създадения/актуализиран User
    public User generateAndSavePlanForUser(String sessionId) {
        SessionState session = sessionMap.get(sessionId);
        if (session == null || !"DONE".equals(session.state)) {
            throw new IllegalStateException("Сесията не е готова за генериране на план или не е намерена.");
        }

        User user;
        if (session.userId != null && !session.isGuest) {
            // Регистриран потребител: Актуализираме съществуващия
            user = userRepository.findById(session.userId)
                    .orElseThrow(() -> new RuntimeException("Регистриран потребител с ID " + session.userId + " не е намерен!"));


        } else {
            // Нов временен потребител (от гост сесия)
            user = new User();

            user.setEmail(session.email != null && !session.email.isEmpty() ? session.email : UUID.randomUUID().toString() + "@guest.com");
            user.setFullName(session.fullName != null && !session.fullName.isEmpty() ? session.fullName : "Guest User");
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Хеширана случайна парола
            user.setIsTemporaryAccount(true); // Маркираме като временен акаунт

            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER не е намерен!"));
            user.setRoles(Set.of(userRole));
        }


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

        // Задаване на списъчни данни
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
                    case "2":
                        displayString = "2 пъти дневно";
                        break;
                    case "3":
                        displayString = "3 пъти дневно";
                        break;
                    case "4":
                        displayString = "4 пъти дневно";
                        break;
                    case "5":
                        displayString = "5 пъти дневно";
                        break;
                    case "6":
                        displayString = "6 пъти дневно";
                        break;
                    default:
                        throw new IllegalArgumentException("Невалидна честота на хранене в сесията: " + session.mealFrequencyPreference);
                }
                user.setMealFrequencyPreference(MealFrequencyPreferenceType.fromString(displayString));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Невалидно предпочитание за честота на хранене в сесията: " + session.mealFrequencyPreference + " - " + e.getMessage(), e);
            }
        }

        // Запазване или актуализиране на потребителя в базата данни
        User savedOrUpdatedUser = userRepository.save(user);


        nutritionPlanService.generateNutritionPlan(savedOrUpdatedUser);

        // Генериране и запазване на тренировъчния план
        trainingPlanService.generateAndSaveTrainingPlanForUser(savedOrUpdatedUser);

        session.planGenerated = true; // Маркираме, че планът е генериран за тази сесия
        session.userId = savedOrUpdatedUser.getId(); // Актуализираме ID на потребителя в сесията, ако е бил нов
        session.isGuest = savedOrUpdatedUser.getIsTemporaryAccount() != null && savedOrUpdatedUser.getIsTemporaryAccount(); // Актуализираме guest статуса

        logger.info("План генериран/актуализиран за потребител: {} (ID: {})", savedOrUpdatedUser.getFullName(), savedOrUpdatedUser.getId());
        return savedOrUpdatedUser; // Връщаме създадения/актуализиран потребител
    }



    private String handleDietExplanation(SessionState session, String message) {
        if (message.trim().equalsIgnoreCase("да")) {
            session.state = "ASK_DIET_TYPE";
            return """
                    📊 <strong>Разбира се, ето кратко резюме на основните диетични подходи:</strong><br><br>
                    
                    🍏 <strong>Балансирана диета:</strong> Фокусира се върху здравословна, разнообразна храна с умерено съотношение на макронутриенти (протеини, въглехидрати, мазнини). Идеална за поддържане на общо здраве и енергийни нива.<br><br>
                    
                    🥩 <strong>Протеинова диета:</strong> Богата на протеини (месо, риба, яйца, млечни продукти) и често по-ниска на въглехидрати. Подходяща за натрупване на мускулна маса, възстановяване и чувство за ситост.<br><br>
                    
                    🥑 <strong>Кетогенна диета:</strong> Силно ограничава въглехидратите, замествайки ги с мазнини. Тялото влиза в състояние на кетоза, което спомага за изгаряне на мазнини и може да е ефективна за отслабване и контрол на кръвната захар.<br><br>
                    
                    🌱 <strong>Веган диета:</strong> Изключва всички животински продукти, включително месо, млечни продукти, яйца и мед. Базира се изцяло на растителни храни, богати на фибри, витамини и минерали.<br><br>
                    
                    🍲 <strong>Вегетарианска диета:</strong> Подобна на веган, но позволява млечни продукти и яйца.<br><br>
                    
                    🌰 <strong>Палео диета:</strong> Фокусира се върху храни, достъпни за палеолитните хора (месо, риба, зеленчуци, плодове, ядки, семена), изключва зърнени култури, бобови растения, млечни продукти и преработени храни.<br><br>
                    
                    <strong>Моля, изберете диета, която най-добре отговаря на вашите нужди:</strong> <em>балансирана / протеинова / кето / веган / вегетарианска / палео</em>
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
        String dietNameInDb;

        switch (input) {
            case "балансирана":
            case "балансирана диета":
            case "стандартна":
            case "стандартна диета":
                dietNameInDb = "Балансирана";
                break;
            case "протеинова":
            case "протеинова диета":
                dietNameInDb = "Протеинова";
                break;
            case "кето":
            case "кетогенна":
            case "кето диета":
                dietNameInDb = "Кето";
                break;
            case "веган":
            case "веганска":
            case "веган диета":
                dietNameInDb = "Веган";
                break;
            case "вегетарианска":
            case "вегетарианска диета":
                dietNameInDb = "Вегетарианска";
                break;
            case "палео":
            case "палео диета":
                dietNameInDb = "Палео";
                break;
            default:
                return "Не разбрах избора Ви. Моля, изберете един от предложените типове диети: **балансирана / протеинова / кето / веган / вегетарианска / палео**";
        }

        Optional<DietType> dietTypeOptional = dietTypeRepository.findByNameIgnoreCase(dietNameInDb);
        if (dietTypeOptional.isPresent()) {
            session.dietType = dietTypeOptional.get().getName();
            session.state = "ASK_WEIGHT";
            return "Моля, въведете вашето текущо тегло **в килограми (кг)**.";
        }
        logger.error("Диетичен тип '{}' не е намерен в системата.", dietNameInDb);
        return "Възникна вътрешен проблем: Диетичен тип '" + dietNameInDb + "' не е намерен в системата. Моля, свържете се с поддръжката или опитайте 'рестарт'.";
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
            case "сваляне на килограми":
            case "редукция":
            case "намаляване на тегло":
                goalName = "Отслабване";
                break;
            case "мускулна маса":
            case "качване":
            case "наддаване":
            case "наддаване на тегло":
            case "напълняване":
                goalName = "Наддаване на тегло";
                break;
            case "поддържане":
            case "поддържане на тегло":
            case "задържане":
                goalName = "Поддържане на тегло";
                break;
            default:
                return "Не разбрах целта Ви. Моля, изберете една от предложените: **отслабване / мускулна маса / поддържане**";
        }

        Optional<Goal> goalOptional = goalRepository.findByNameIgnoreCase(goalName);
        if (goalOptional.isPresent()) {
            session.goal = goalOptional.get().getName();
            session.state = "ASK_ACTIVITY_LEVEL";
            return "Колко активен сте през деня? **заседнал / леко активен / умерено активен / много активен / изключително активен**";
        }
        logger.error("Цел '{}' не е намерена в системата.", goalName);
        return "Възникна вътрешен проблем: Цел '" + goalName + "' не е намерена в системата. Моля, свържете се с поддръжката или опитайте 'рестарт'.";
    }

    private String handleActivityLevelInput(SessionState session, String message) {
        String input = message.trim().toLowerCase();
        String level;

        switch (input) {
            case "заседнал":
            case "много малко":
                level = "Заседнал";
                break;
            case "леко активен":
            case "леко":
                level = "Леко активен";
                break;
            case "умерено активен":
            case "умерено":
                level = "Умерено активен";
                break;
            case "много активен":
            case "активен":
                level = "Много активен";
                break;
            case "изключително активен":
            case "много много активен":
                level = "Изключително активен";
                break;
            default:
                return "Не разбрах нивото на активност. Моля, изберете: **заседнал / леко активен / умерено активен / много активен / изключително активен**";
        }

        Optional<ActivityLevel> activityLevel = activityLevelRepository.findByNameIgnoreCase(level);
        if (activityLevel.isPresent()) {
            session.activityLevel = activityLevel.get().getName();
            session.state = "ASK_MEAT_PREFERENCE";
            return "Имате ли предпочитания относно консумацията на месо? **пилешко / телешко / риба / свинско / агнешко / без месо / няма значение**";
        }
        logger.error("Ниво на активност '{}' не е намерено в системата.", level);
        return "Възникна вътрешен проблем: Ниво на активност '" + level + "' не е намерено в системата. Моля, свържете се с поддръжката или опитайте 'рестарт'.";
    }

    private String handleMeatPreference(SessionState session, String message) {
        String input = message.trim().toLowerCase();
        List<String> validPreferences = Arrays.asList("пилешко", "телешко", "риба", "свинско", "агнешко", "без месо", "няма значение");

        if (validPreferences.contains(input)) {
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
            if (duration >= 15 && duration <= 180) {
                session.trainingDurationMinutes = duration;
                session.state = "ASK_LEVEL";
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

    // МОДИФИЦИРАН МЕТОД: handleMealFrequency - вече връща Map<String, Object> със FullPlanDTO
    private Map<String, Object> handleMealFrequency(String sessionId, SessionState session, String message) {
        String input = message.trim();
        try {
            int frequency = Integer.parseInt(input);
            if (frequency >= 2 && frequency <= 6) {
                session.mealFrequencyPreference = input;
                session.state = "DONE"; // <-- ЗАДАВАМЕ СЪСТОЯНИЕТО НА DONE

                // ГЕНЕРИРАНЕ И ЗАПАЗВАНЕ НА ПОТРЕБИТЕЛЯ И ПЛАНОВЕТЕ
                // generateAndSavePlanForUser вече запазва потребителя и генерира плановете
                User savedOrUpdatedUser = generateAndSavePlanForUser(sessionId);

                // ИЗВЛИЧАНЕ НА FullPlanDTO чрез NutritionPlanService
                FullPlanDTO fullPlanDTO = nutritionPlanService.getFullPlanByUserId(savedOrUpdatedUser.getId());

                if (fullPlanDTO == null) {
                    logger.error("Неуспешно извличане на FullPlanDTO за потребител ID: {}", savedOrUpdatedUser.getId());
                    return Map.of("type", "error", "message", "Възникна грешка при извличане на генерирания план.");
                }

                // ВРЪЩАНЕ НА FullPlanDTO В ОТГОВОР
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("type", "plan"); // Използваме "plan", както очаква фронтенда
                responseMap.put("message", "Вашият персонализиран план е успешно генериран и запазен!");
                responseMap.put("isGuest", session.isGuest);
                responseMap.put("userId", session.userId);
                responseMap.put("plan", fullPlanDTO); // <-- ДОБАВЯМЕ ЦЕЛИЯ FullPlanDTO ТУК

                return responseMap;
            } else {
                return Map.of("type", "text", "message", "Невалидна честота на хранене. Моля, въведете число между 2 и 6.");
            }
        } catch (NumberFormatException e) {
            return Map.of("type", "text", "message", "Невалиден формат. Моля, въведете число.");
        } catch (Exception e) {
            logger.error("Възникна грешка при генерирането на плана за сесия {}: {}", sessionId, e.getMessage(), e);
            return Map.of("type", "error", "message", "Възникна грешка при генерирането на плана: " + e.getMessage());
        }
    }

}