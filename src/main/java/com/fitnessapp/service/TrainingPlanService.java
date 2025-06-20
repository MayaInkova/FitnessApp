package com.fitnessapp.service;

import com.fitnessapp.dto.ExerciseDTO;
import com.fitnessapp.dto.TrainingPlanDTO;
import com.fitnessapp.dto.TrainingPlanHistoryDTO;
import com.fitnessapp.dto.TrainingSessionDTO;
import com.fitnessapp.model.*;
import com.fitnessapp.repository.ExerciseRepository;
import com.fitnessapp.repository.TrainingPlanRepository;
import com.fitnessapp.repository.TrainingSessionRepository;
import com.fitnessapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingPlanService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingPlanService.class);

    private final TrainingPlanRepository trainingPlanRepository;
    private final ExerciseRepository exerciseRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final UserRepository userRepository;

    /**
     * Генерира и запазва тренировъчен план за потребител.
     * Този метод е основният за генериране на план.
     * @param user Потребителят, за когото се генерира планът.
     * @return Запазеният TrainingPlan entity.
     */
    @Transactional
    public TrainingPlan generateAndSaveTrainingPlanForUser(User user) {
        validateUserProfileForTrainingPlanGeneration(user);

        Optional<TrainingPlan> existingPlan =
                trainingPlanRepository.findByUserAndDateGenerated(user, LocalDate.now());
        if (existingPlan.isPresent()) {
            logger.info("Existing training plan found for user {} for today. Returning existing plan.", user.getFullName());
            TrainingPlan plan = existingPlan.get();
            initializeTrainingPlanLazyFields(plan);
            return plan;
        }

        LevelType userLevel = user.getLevel();
        TrainingType userTrainingType = user.getTrainingType();

        TrainingPlan trainingPlan = TrainingPlan.builder()
                .user(user)
                .dateGenerated(LocalDate.now())
                .daysPerWeek(Optional.ofNullable(user.getTrainingDaysPerWeek()).orElse(getDefaultTrainingDays(userLevel)))
                .durationMinutes(Optional.ofNullable(user.getTrainingDurationMinutes()).orElse(getDefaultTrainingDuration(userLevel)))
                .trainingSessions(new ArrayList<>())
                .build();

        trainingPlan.setUserGenderSnapshot(user.getGender());
        trainingPlan.setUserAgeSnapshot(user.getAge());
        trainingPlan.setUserWeightSnapshot(user.getWeight());
        trainingPlan.setUserHeightSnapshot(user.getHeight());
        // Проверете дали ActivityLevel и Goal са Lazy-зареждащи се в User entity.
        // Ако са LAZY, трябва да се инициализират преди да се използват за snapshot.
        // Hibernate.initialize(user.getActivityLevel()); // Разкоментирайте, ако е Lazy
        // Hibernate.initialize(user.getGoal()); // Разкоментирайте, ако е Lazy
        trainingPlan.setUserActivityLevelSnapshot(user.getActivityLevel());
        trainingPlan.setUserTrainingTypeSnapshot(user.getTrainingType());
        trainingPlan.setUserLevelSnapshot(user.getLevel());
        trainingPlan.setUserGoalSnapshot(user.getGoal());


        // Запазваме плана, за да получим ID, преди да добавяме сесии и упражнения
        trainingPlan = trainingPlanRepository.save(trainingPlan);

        int daysPerWeek = trainingPlan.getDaysPerWeek();
        int durationMinutesPerSession = trainingPlan.getDurationMinutes();
        Random random = new Random();

        List<com.fitnessapp.model.DayOfWeek> selectedDays = selectTrainingDays(daysPerWeek);

        for (com.fitnessapp.model.DayOfWeek day : selectedDays) {
            TrainingSession session = TrainingSession.builder()
                    .dayOfWeek(day)
                    .durationMinutes(durationMinutesPerSession)
                    .trainingPlan(trainingPlan) // Важно: задаваме връзката към плана
                    .exercises(new ArrayList<>()) // Инициализирайте празен списък за упражненията на сесията
                    .build();

            List<Exercise> exercises = getExercisesForUser(userLevel, userTrainingType);
            if (exercises.isEmpty()) {
                logger.warn("No available exercises for DifficultyLevel: {} and TrainingType {}. Saving empty session for day {}.", userLevel, userTrainingType, day);
                session = trainingSessionRepository.save(session);
                trainingPlan.addTrainingSession(session);
                continue; // Move to next day
            }

            // --- МОДИФИЦИРАН И ПОДОБРЕН БЛОК ЗА ИЗБОР НА УПРАЖНЕНИЯ ---
            List<Exercise> currentSessionExercises = new ArrayList<>();
            List<Exercise> availableExercisesForSession = new ArrayList<>(exercises);
            Collections.shuffle(availableExercisesForSession);

            int currentSessionTime = 0;
            final int TARGET_DURATION = durationMinutesPerSession;
            final int MIN_EXERCISES_PER_SESSION = 3;
            final int MAX_OVERAGE_MINUTES_SOFT_CAP = 15; // How much we can go over target for a "good" session
            final int HARD_CAP_EXERCISES = 8; // Prevent excessively long lists
            final int MIN_DURATION_PER_EXERCISE = 1; // Lowered to 1 min for greater leniency in finding exercises

            // Pass 1: Try to fill with a good fit, prioritizing minimum exercises and staying near target
            // Iterate through a shuffled copy to pick from.
            List<Exercise> tempPass1Exercises = new ArrayList<>(availableExercisesForSession); // Copy for safe iteration/removal
            Iterator<Exercise> it = tempPass1Exercises.iterator();

            while (it.hasNext() && currentSessionExercises.size() < HARD_CAP_EXERCISES) {
                Exercise potentialExercise = it.next();

                // Skip invalid exercises (null or below minimum duration)
                if (potentialExercise.getDurationMinutes() == null || potentialExercise.getDurationMinutes() < MIN_DURATION_PER_EXERCISE) {
                    logger.debug("Skipping exercise with invalid or too short duration: {} (ID: {}). Duration: {}", potentialExercise.getName(), potentialExercise.getId(), potentialExercise.getDurationMinutes());
                    continue;
                }

                int exerciseDuration = potentialExercise.getDurationMinutes();

                // Conditions for adding in Pass 1:
                // 1. If we are under the target duration AND adding this exercise doesn't overshoot much.
                // 2. OR if we are still below the MIN_EXERCISES_PER_SESSION and adding this exercise doesn't massively overshoot.
                boolean fitsWell = (currentSessionTime + exerciseDuration <= TARGET_DURATION + MAX_OVERAGE_MINUTES_SOFT_CAP);
                boolean needsMinExercises = (currentSessionExercises.size() < MIN_EXERCISES_PER_SESSION && currentSessionTime + exerciseDuration <= TARGET_DURATION + MAX_OVERAGE_MINUTES_SOFT_CAP * 2); // More permissive for min count

                if (fitsWell || needsMinExercises || currentSessionExercises.isEmpty()) { // currentSessionExercises.isEmpty() handles the very first exercise
                    currentSessionExercises.add(potentialExercise);
                    currentSessionTime += exerciseDuration;
                    it.remove(); // Remove from temp list for next iteration of this pass
                }

                // Break conditions for Pass 1:
                // If we've hit target duration AND minimum exercises, OR filled up to a reasonable amount.
                if (currentSessionTime >= TARGET_DURATION && currentSessionExercises.size() >= MIN_EXERCISES_PER_SESSION) {
                    logger.info("Pass 1: Session for day {} filled optimally. Exercises: {}, Time: {} min (Target: {} min).", day, currentSessionExercises.size(), currentSessionTime, TARGET_DURATION);
                    break;
                }
            }

            // Pass 2: Fallback to ensure *at least* MIN_EXERCISES_PER_SESSION if not met,
            // or if the session is still considerably under target duration.
            // This pass is less strict on overshoot to guarantee content.
            // Use the remaining exercises from availableExercisesForSession (original list without selected)
            // or if tempPass1Exercises was mutated, re-init with allAvailableExercises for the current session.
            List<Exercise> remainingExercises = new ArrayList<>(exercises); // Use fresh copy for robustness
            remainingExercises.removeAll(currentSessionExercises); // Exclude already chosen exercises
            Collections.shuffle(remainingExercises); // Shuffle remaining for new selection chances

            if (currentSessionExercises.size() < MIN_EXERCISES_PER_SESSION || currentSessionTime < TARGET_DURATION - MAX_OVERAGE_MINUTES_SOFT_CAP) {
                logger.warn("Pass 2: Session for day {} still underfilled ({} exercises, {} min). Target: {} min. Attempting fallback fill.",
                        day, currentSessionExercises.size(), currentSessionTime, TARGET_DURATION);

                for (Exercise fbExercise : remainingExercises) {
                    if (fbExercise.getDurationMinutes() == null || fbExercise.getDurationMinutes() < MIN_DURATION_PER_EXERCISE) {
                        continue;
                    }
                    if (!currentSessionExercises.contains(fbExercise) && currentSessionExercises.size() < HARD_CAP_EXERCISES) { // Avoid duplicates and hard cap
                        currentSessionExercises.add(fbExercise);
                        currentSessionTime += fbExercise.getDurationMinutes();

                        // Break conditions for Pass 2:
                        // Met min exercises AND duration is at least half of target, OR hit hard cap on exercises.
                        if ((currentSessionExercises.size() >= MIN_EXERCISES_PER_SESSION && currentSessionTime >= TARGET_DURATION / 2) ||
                                currentSessionExercises.size() >= HARD_CAP_EXERCISES) {
                            logger.info("Pass 2: Fallback succeeded for day {}. Exercises: {}, Time: {} min.", day, currentSessionExercises.size(), currentSessionTime);
                            break;
                        }
                    }
                }
            }

            // Final GUARANTEE: If the session is still empty for ANY reason, FORCE at least one valid exercise.
            // This is the unbreakable safety net.
            if (currentSessionExercises.isEmpty() && !exercises.isEmpty()) {
                logger.error("CRITICAL SAFETY NET: Session for day {} is STILL EMPTY after all advanced attempts! Forcing one exercise.", day);
                // Get the first available exercise directly, regardless of its duration, just to ensure content.
                // We ensure 'exercises' is not empty due to the outer check at the beginning of the loop.
                Exercise arbitraryExercise = exercises.stream()
                        .filter(e -> e.getDurationMinutes() != null) // Only ensure not null
                        .findFirst()
                        .orElse(null); // Fallback to null if no exercise has non-null duration (should not happen if DB is seeded)

                if (arbitraryExercise != null) {
                    currentSessionExercises.add(arbitraryExercise);
                    currentSessionTime += (arbitraryExercise.getDurationMinutes() != null ? arbitraryExercise.getDurationMinutes() : 0); // Add duration if valid
                    logger.info("Critical safety net added exercise '{}' ({} min) to session for day {}. Total exercises: {}", arbitraryExercise.getName(), arbitraryExercise.getDurationMinutes(), day, currentSessionExercises.size());
                } else {
                    logger.error("NO VALID EXERCISES FOUND IN DATABASE TO USE AS ARBITRARY FALLBACK. This indicates a severe data problem. Please check your seeded exercise data for day {}.", day);
                }
            } else if (currentSessionExercises.isEmpty()) {
                logger.error("Session for day {} remains EMPTY and there are NO exercises available at all. This indicates a severe data problem.", day);
            }


            // Log final state for session, regardless of outcome
            logger.info("Session for day {} finalized with {} exercises, total time: {} min (Target: {} min).", day, currentSessionExercises.size(), currentSessionTime, TARGET_DURATION);
            // --- КРАЙ НА МОДИФИЦИРАНИЯ И ПОДОБРЕН БЛОК ЗА ИЗБОР НА УПРАЖНЕНИЯ ---

            // Добавяме събраните упражнения към сесията
            currentSessionExercises.forEach(session::addExercise);

            session = trainingSessionRepository.save(session);
            trainingPlan.addTrainingSession(session); // Добавяме към колекцията на плана (важно за JPA)
        }

        TrainingPlan savedPlan = trainingPlanRepository.save(trainingPlan);
        logger.info("Successfully generated and saved training plan #{} for user {}.", savedPlan.getId(), user.getFullName());
        return savedPlan;
    }

    /**
     * Помощен метод за извличане на упражнения въз основа на нивото на потребителя и типа тренировка.
     * @param level Нивото на потребителя.
     * @param type Типът тренировка.
     * @return Списък с филтрирани упражнения.
     */
    private List<Exercise> getExercisesForUser(LevelType level, TrainingType type) {
        DifficultyLevel diff = getDifficultyLevelEnum(level);
        ExerciseType exType = getExerciseTypeEnum(type);

        // Първи опит: по трудност и тип
        List<Exercise> filtered = exerciseRepository.findByDifficultyLevelAndType(diff, exType);
        if (!filtered.isEmpty()) {
            logger.debug("Found {} exercises for DifficultyLevel: {} and ExerciseType: {}.", filtered.size(), diff, exType);
            return filtered;
        }

        logger.warn("No exercises found for DifficultyLevel: {} and ExerciseType: {}. Trying by Type only.", diff, exType);
        // Втори опит: само по тип
        List<Exercise> byType = exerciseRepository.findByType(exType);
        if (!byType.isEmpty()) {
            logger.debug("Found {} exercises for ExerciseType: {}.", byType.size(), exType);
            return byType;
        }

        logger.warn("No exercises found for ExerciseType: {}. Returning all available exercises.", exType);
        // Трети опит: всички упражнения
        List<Exercise> allExercises = exerciseRepository.findAll();
        if (allExercises.isEmpty()) {
            logger.error("CRITICAL: No exercises found in the database at all. Please ensure exercises are seeded.");
        }
        return allExercises;
    }

    /**
     * Валидира потребителския профил за наличие на основни данни,
     * необходими за генериране на тренировъчен план.
     * @param user Потребителят за валидация.
     * @throws IllegalArgumentException Ако липсват необходими данни.
     */
    private void validateUserProfileForTrainingPlanGeneration(User user) {
        List<String> missing = new ArrayList<>();
        if (user.getGender() == null) missing.add("пол");
        if (user.getAge() == null) missing.add("възраст");
        if (user.getHeight() == null) missing.add("ръст");
        if (user.getWeight() == null) missing.add("тегло");
        if (user.getActivityLevel() == null) missing.add("ниво на активност");
        if (user.getGoal() == null) missing.add("цел");
        if (user.getTrainingType() == null) missing.add("тип тренировка");
        if (user.getLevel() == null) missing.add("ниво на подготовка");
        if (user.getTrainingDaysPerWeek() == null) missing.add("тренировъчни дни");
        if (user.getTrainingDurationMinutes() == null) missing.add("продължителност");

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Попълнете липсващите полета за генериране на тренировъчен план: " + String.join(", ", missing));
        }
    }

    /**
     * Избира случайни дни за тренировка през седмицата.
     * @param days Брой тренировъчни дни.
     * @return Списък от DayOfWeek енуми.
     */
    private List<com.fitnessapp.model.DayOfWeek> selectTrainingDays(int days) {
        int numDays = Math.min(days, 7); // Уверете се, че не надвишаваме 7 дни
        List<java.time.DayOfWeek> allJavaTimeDays = new ArrayList<>(Arrays.asList(java.time.DayOfWeek.values()));
        Collections.shuffle(allJavaTimeDays);

        return allJavaTimeDays.stream()
                .limit(numDays)
                .map(javaTimeDay -> com.fitnessapp.model.DayOfWeek.valueOf(javaTimeDay.name()))
                .collect(Collectors.toList());
    }

    /**
     * Връща брой тренировъчни дни по подразбиране въз основа на нивото на потребителя.
     * @param level Нивото на потребителя.
     * @return Брой дни.
     */
    private int getDefaultTrainingDays(LevelType level) {
        return switch (level) {
            case BEGINNER -> 3;
            case INTERMEDIATE -> 4;
            case ADVANCED -> 5;
            // default клона е излишен, ако всички енум стойности са покрити
            // default -> 3;
        };
    }

    /**
     * Връща продължителност на тренировка по подразбиране въз основа на нивото на потребителя.
     * @param level Нивото на потребителя.
     * @return Продължителност в минути.
     */
    private int getDefaultTrainingDuration(LevelType level) {
        return switch (level) {
            case BEGINNER -> 45;
            case INTERMEDIATE -> 60;
            case ADVANCED -> 75;
            // default клона е излишен
            // default -> 60;
        };
    }

    /**
     * Конвертира LevelType в DifficultyLevel.
     * @param level LevelType.
     * @return DifficultyLevel.
     */
    private DifficultyLevel getDifficultyLevelEnum(LevelType level) {
        return switch (level) {
            case BEGINNER -> DifficultyLevel.BEGINNER;
            case INTERMEDIATE -> DifficultyLevel.INTERMEDIATE;
            case ADVANCED -> DifficultyLevel.ADVANCED;
            // default клона е излишен
            // default -> DifficultyLevel.BEGINNER;
        };
    }

    /**
     * Конвертира TrainingType в ExerciseType.
     * @param type TrainingType.
     * @return ExerciseType.
     */
    private ExerciseType getExerciseTypeEnum(TrainingType type) {
        return switch (type) {
            case WEIGHTS -> ExerciseType.WEIGHTS;
            case BODYWEIGHT -> ExerciseType.BODYWEIGHT;
            case CARDIO -> ExerciseType.CARDIO;
            // default клона е излишен
            // default -> ExerciseType.BODYWEIGHT;
        };
    }

    /**
     * Конвертира TrainingPlan entity в TrainingPlanDTO.
     * @param plan TrainingPlan entity.
     * @return TrainingPlanDTO обект.
     */
    @Transactional(readOnly = true) // Транзакцията е важна за initialize
    public TrainingPlanDTO convertTrainingPlanToDTO(TrainingPlan plan) {
        if (plan == null) return null;
        initializeTrainingPlanLazyFields(plan); // Инициализирайте всички Lazy полета

        List<TrainingSessionDTO> sessionDTOs = plan.getTrainingSessions().stream()
                .map(this::convertSessionToDTO)
                .collect(Collectors.toList());

        return TrainingPlanDTO.builder()
                .id(plan.getId())
                .dateGenerated(plan.getDateGenerated())
                .daysPerWeek(plan.getDaysPerWeek())
                .durationMinutes(plan.getDurationMinutes())
                .userId(plan.getUser() != null ? plan.getUser().getId() : null)
                .userEmail(plan.getUser() != null ? plan.getUser().getEmail() : null)
                .trainingSessions(sessionDTOs)
                .userGenderSnapshot(plan.getUserGenderSnapshot() != null ? plan.getUserGenderSnapshot().name() : null)
                .userAgeSnapshot(plan.getUserAgeSnapshot())
                .userWeightSnapshot(plan.getUserWeightSnapshot())
                .userHeightSnapshot(plan.getUserHeightSnapshot())
                .userActivityLevelSnapshotName(plan.getUserActivityLevelSnapshot() != null ? plan.getUserActivityLevelSnapshot().getName() : null)
                .userTrainingTypeSnapshot(plan.getUserTrainingTypeSnapshot())
                .userLevelSnapshot(plan.getUserLevelSnapshot())
                .userGoalNameSnapshot(plan.getUserGoalSnapshot() != null ? plan.getUserGoalSnapshot().getName() : null)
                .build();
    }

    /**
     * Конвертира TrainingSession entity в TrainingSessionDTO.
     * @param session TrainingSession entity.
     * @return TrainingSessionDTO обект.
     */
    private TrainingSessionDTO convertSessionToDTO(TrainingSession session) {
        if (session == null) return null;
        Hibernate.initialize(session.getExercises()); // Инициализирайте упражненията в сесията

        List<ExerciseDTO> exerciseDTOs = session.getExercises().stream()
                .map(this::convertExerciseToDTO) // Използваме нов помощен метод за конверсия на Exercise
                .collect(Collectors.toList());

        return TrainingSessionDTO.builder()
                .id(session.getId())
                .dayOfWeek(session.getDayOfWeek())
                .durationMinutes(session.getDurationMinutes())
                .exercises(exerciseDTOs)
                .build();
    }

    /**
     * Конвертира Exercise entity в ExerciseDTO.
     * @param ex Exercise entity.
     * @return ExerciseDTO обект.
     */
    private ExerciseDTO convertExerciseToDTO(Exercise ex) {
        if (ex == null) return null;
        return ExerciseDTO.builder()
                .id(ex.getId())
                .name(ex.getName())
                .description(ex.getDescription())
                .sets(ex.getSets())
                .reps(ex.getReps())
                .durationMinutes(ex.getDurationMinutes())
                .type(ex.getType())
                .difficultyLevel(ex.getDifficultyLevel())
                .equipment(ex.getEquipment())
                .build();
    }

    /**
     * Извлича всички тренировъчни планове от базата данни.
     * @return Списък от DTO обекти на всички тренировъчни планове.
     */
    @Transactional(readOnly = true)
    public List<TrainingPlanDTO> getAllTrainingPlansDTO() {
        return trainingPlanRepository.findAll().stream()
                .map(this::convertTrainingPlanToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Генерира и запазва тренировъчен план за потребител, връщайки DTO.
     * @param user Потребителят, за когото се генерира планът.
     * @return DTO обект на генерирания план.
     */
    @Transactional
    public TrainingPlanDTO generateAndSaveTrainingPlanForUserDTO(User user) {
        // Използваме вече съществуващия метод за генериране и запазване на entity
        TrainingPlan plan = generateAndSaveTrainingPlanForUser(user);
        // След това конвертираме entity в DTO и го връщаме
        return convertTrainingPlanToDTO(plan);
    }

    /**
     * Извлича всички тренировъчни планове за даден потребител.
     * @param user Потребителят.
     * @return Списък от DTO обекти на тренировъчни планове, сортирани по дата (най-новите първи).
     */
    @Transactional(readOnly = true)
    public List<TrainingPlanDTO> getTrainingPlansByUserDTO(User user) {
        // Уверете се, че работите с managed User entity в рамките на транзакцията
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + user.getId() + " not found."));

        List<TrainingPlan> plans = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(managedUser);
        return plans.stream()
                .map(this::convertTrainingPlanToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Поправя липсващи тренировъчни планове за потребители, ако няма такъв за текущия ден.
     */
    @Transactional
    public void fixMissingTrainingPlans() {
        logger.info("Fixing missing training plans...");
        userRepository.findAll().forEach(user -> {
            if (trainingPlanRepository.findByUserAndDateGenerated(user, LocalDate.now()).isEmpty()) {
                try {
                    validateUserProfileForTrainingPlanGeneration(user);
                    TrainingPlan plan = generateAndSaveTrainingPlanForUser(user);
                    logger.info("Generated training plan for user: {}", user.getEmail());
                } catch (IllegalArgumentException e) {
                    logger.warn("Skipping training plan generation for user {} due to missing profile fields: {}", user.getEmail(), e.getMessage());
                } catch (Exception e) {
                    logger.error("Error generating training plan for user {}: {}", user.getEmail(), e.getMessage(), e);
                }
            } else {
                logger.debug("Training plan already exists for user {} for today. Skipping generation.", user.getEmail());
            }
        });
    }

    /**
     * Връща препоръчителен тренировъчен план. (Място за имплементация)
     * @param goal Целта на тренировката.
     * @param withWeights Дали да включва тежести.
     * @return TrainingPlanDTO.
     */
    @Transactional(readOnly = true)
    public TrainingPlanDTO getRecommendedTrainingPlanDTO(String goal, boolean withWeights) {
        logger.warn("Not implemented: getRecommendedTrainingPlanDTO. This method needs specific logic.");
        // Тази логика би била много специфична за вашето приложение
        // Пример:
        // List<TrainingPlan> plans = trainingPlanRepository.findByGoalAndTrainingType(...);
        // return plans.stream().findFirst().map(this::convertTrainingPlanToDTO).orElse(null);
        return null;
    }

    /**
     * Извлича историята на тренировъчните планове за даден потребител.
     * @param userId ID на потребителя.
     * @return Списък от DTO обекти за история на тренировъчни планове.
     */
    @Transactional(readOnly = true)
    public List<TrainingPlanHistoryDTO> getTrainingPlanHistory(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        List<TrainingPlan> plans = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user);

        return plans.stream()
                .map(plan -> {
                    // Инициализирайте всички lazy полета, които ще се използват в DTO
                    initializeTrainingPlanLazyFields(plan);

                    return TrainingPlanHistoryDTO.builder()
                            .id(plan.getId())
                            .dateGenerated(plan.getDateGenerated())
                            .trainingPlanDescription(getTrainingPlanSummary(plan)) // ИЗПОЛЗВАЙТЕ ПОМОЩЕН МЕТОД
                            .trainingDaysPerWeek(plan.getDaysPerWeek())
                            .trainingDurationMinutes(plan.getDurationMinutes())
                            .userGenderSnapshot(plan.getUserGenderSnapshot() != null ? plan.getUserGenderSnapshot().name() : null)
                            .userAgeSnapshot(plan.getUserAgeSnapshot())
                            .userWeightSnapshot(plan.getUserWeightSnapshot())
                            .userHeightSnapshot(plan.getUserHeightSnapshot())
                            .userActivityLevelSnapshotName(plan.getUserActivityLevelSnapshot() != null ? plan.getUserActivityLevelSnapshot().getName() : null)
                            .userTrainingTypeSnapshot(plan.getUserTrainingTypeSnapshot())
                            .userLevelSnapshot(plan.getUserLevelSnapshot())
                            .userGoalNameSnapshot(plan.getUserGoalSnapshot() != null ? plan.getUserGoalSnapshot().getName() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Помощен метод за инициализация на Lazy полета на TrainingPlan entity.
     * @param plan TrainingPlan entity.
     */
    private void initializeTrainingPlanLazyFields(TrainingPlan plan) {
        if (plan == null) return;
        Hibernate.initialize(plan.getUser());
        Hibernate.initialize(plan.getTrainingSessions());
        // За всяка сесия, инициализирайте упражненията й
        if (plan.getTrainingSessions() != null) {
            plan.getTrainingSessions().forEach(session -> Hibernate.initialize(session.getExercises()));
        }
        // Ако ActivityLevel, TrainingType и Goal са Lazy-зареждащи се, инициализирайте ги:
        Hibernate.initialize(plan.getUserActivityLevelSnapshot());
        // Hibernate.initialize(plan.getUserTrainingTypeSnapshot()); // Активирай, ако е Lazy
        // Hibernate.initialize(plan.getUserLevelSnapshot()); // Активирай, ако е Lazy
        Hibernate.initialize(plan.getUserGoalSnapshot());
    }

    /**
     * Изчислява кратко описание на тренировъчния план.
     * Можете да адаптирате това според информацията, която искате да покажете.
     * @param plan Тренировъчен план.
     * @return Кратко текстово описание.
     */
    private String getTrainingPlanSummary(TrainingPlan plan) {
        if (plan == null) {
            return "Няма информация за тренировъчен план.";
        }
        String summary = String.format("План генериран на %s за %d дни/седмица, %d минути на сесия.",
                plan.getDateGenerated(), plan.getDaysPerWeek(), plan.getDurationMinutes());

        if (plan.getUserTrainingTypeSnapshot() != null) {
            summary += " Тип: " + plan.getUserTrainingTypeSnapshot().name() + ".";
        }
        if (plan.getUserLevelSnapshot() != null) {
            summary += " Ниво: " + plan.getUserLevelSnapshot().name() + ".";
        }
        return summary;
    }
}