package com.fitnessapp.service;


import com.fitnessapp.model.Exercise;
import com.fitnessapp.model.TrainingPlan;
import com.fitnessapp.model.TrainingSession;
import com.fitnessapp.model.User;
import com.fitnessapp.model.DifficultyLevel;
import com.fitnessapp.model.ExerciseType;
import com.fitnessapp.model.LevelType;
import com.fitnessapp.model.TrainingType;

import com.fitnessapp.repository.TrainingPlanRepository;
import com.fitnessapp.repository.ExerciseRepository;
import com.fitnessapp.repository.TrainingSessionRepository;
import com.fitnessapp.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class TrainingPlanService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingPlanService.class);

    private final TrainingPlanRepository trainingPlanRepository;
    private final ExerciseRepository exerciseRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final UserRepository userRepository;

    @Autowired
    public TrainingPlanService(
            TrainingPlanRepository trainingPlanRepository,
            ExerciseRepository exerciseRepository,
            TrainingSessionRepository trainingSessionRepository,
            UserRepository userRepository) {
        this.trainingPlanRepository = trainingPlanRepository;
        this.exerciseRepository = exerciseRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TrainingPlan generateAndSaveTrainingPlanForUser(User user) {
        Optional<TrainingPlan> existingPlan =
                trainingPlanRepository.findByUserAndDateGenerated(user, LocalDate.now());
        if (existingPlan.isPresent()) {
            logger.info(
                    "Existing training plan found for user {}. Returning existing plan.",
                    user.getFullName());
            return existingPlan.get();
        }

        LevelType userLevel = user.getLevel();
        TrainingType userTrainingType = user.getTrainingType();

        TrainingPlan trainingPlan =
                TrainingPlan.builder()
                        .user(user)
                        .dateGenerated(LocalDate.now())
                        .daysPerWeek(
                                user.getTrainingDaysPerWeek() != null
                                        ? user.getTrainingDaysPerWeek()
                                        : getDefaultTrainingDays(userLevel))
                        .durationMinutes(
                                user.getTrainingDurationMinutes() != null
                                        ? user.getTrainingDurationMinutes()
                                        : getDefaultTrainingDuration(userLevel))
                        .build();

        int daysPerWeek = trainingPlan.getDaysPerWeek();
        int durationMinutesPerSession = trainingPlan.getDurationMinutes();
        Random random = new Random();

        List<DayOfWeek> selectedTrainingDays = selectTrainingDays(daysPerWeek);

        for (DayOfWeek day : selectedTrainingDays) {
            TrainingSession session =
                    TrainingSession.builder()
                            .trainingPlan(trainingPlan)
                            .dayOfWeek(day)
                            .durationMinutes(durationMinutesPerSession)
                            .build();

            List<Exercise> availableExercises;
            List<Exercise> filteredByLevelAndType =
                    exerciseRepository.findByDifficultyLevelAndType(
                            getDifficultyLevelEnum(userLevel), getExerciseTypeEnum(userTrainingType));

            if (!filteredByLevelAndType.isEmpty()) {
                availableExercises = filteredByLevelAndType;
            } else {
                List<Exercise> filteredByType =
                        exerciseRepository.findByType(getExerciseTypeEnum(userTrainingType));
                if (!filteredByType.isEmpty()) {
                    availableExercises = filteredByType;
                } else {
                    availableExercises = exerciseRepository.findAll();
                }
            }

            int numExercises = Math.min(availableExercises.size(), 5 + random.nextInt(4));
            Collections.shuffle(availableExercises);

            for (int j = 0; j < numExercises; j++) {
                if (j < availableExercises.size()) {
                    session.addExercise(availableExercises.get(j));
                }
            }
            trainingPlan.addTrainingSession(session);
        }

        return trainingPlanRepository.save(trainingPlan);
    }

    private List<DayOfWeek> selectTrainingDays(int daysPerWeek) {
        List<DayOfWeek> allDays =
                Arrays.asList(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY,
                        DayOfWeek.SATURDAY,
                        DayOfWeek.SUNDAY);
        Collections.shuffle(allDays);
        return allDays.subList(0, daysPerWeek);
    }

    private Integer getDefaultTrainingDays(LevelType userLevel) {
        if (userLevel == null) {
            return 3;
        }
        return switch (userLevel) {
            case BEGINNER -> 3;
            case INTERMEDIATE -> 4;
            case ADVANCED -> 5;
        };
    }

    private Integer getDefaultTrainingDuration(LevelType userLevel) {
        if (userLevel == null) {
            return 60;
        }
        return switch (userLevel) {
            case BEGINNER -> 45;
            case INTERMEDIATE -> 60;
            case ADVANCED -> 75;
        };
    }

    private DifficultyLevel getDifficultyLevelEnum(LevelType levelType) {
        if (levelType == null) {
            return DifficultyLevel.BEGINNER;
        }
        return switch (levelType) {
            case BEGINNER -> DifficultyLevel.BEGINNER;
            case INTERMEDIATE -> DifficultyLevel.INTERMEDIATE;
            case ADVANCED -> DifficultyLevel.ADVANCED;
        };
    }

    private ExerciseType getExerciseTypeEnum(TrainingType trainingType) {
        if (trainingType == null) {
            return ExerciseType.WEIGHTS;
        }
        return switch (trainingType) {
            case WEIGHTS -> ExerciseType.WEIGHTS;
            case BODYWEIGHT -> ExerciseType.BODYWEIGHT;
            case CARDIO -> ExerciseType.CARDIO;
        };
    }

    @Transactional
    public void fixMissingTrainingPlans() {
        logger.info("Starting to fix missing training plans.");
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            Optional<TrainingPlan> existingPlan = trainingPlanRepository.findByUserAndDateGenerated(user, LocalDate.now());
            if (existingPlan.isEmpty()) {
                logger.info("No training plan found for user {} for today. Generating a new one.", user.getFullName());
                try {
                    generateAndSaveTrainingPlanForUser(user); // Generate and save a new plan
                } catch (Exception e) {
                    logger.error("Failed to generate training plan for user {}: {}", user.getFullName(), e.getMessage(), e);
                }
            } else {
                logger.info("Training plan already exists for user {} for today.", user.getFullName());
            }
        }
        logger.info("Finished fixing missing training plans.");
    }

    public List<TrainingPlan> getAll() {
        return trainingPlanRepository.findAll();
    }

    public TrainingPlan getRecommended(String goal, boolean withWeights) {
        logger.warn("getRecommended method is called but not fully implemented.");
        return null;
    }

    public List<TrainingPlan> getTrainingPlansByUser(User user) {
        return trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user);
    }
}