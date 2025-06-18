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
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrainingPlanService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingPlanService.class);

    private final TrainingPlanRepository trainingPlanRepository;
    private final ExerciseRepository exerciseRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final UserRepository userRepository;

    @Autowired
    public TrainingPlanService(TrainingPlanRepository trainingPlanRepository,
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
        validateUserProfileForTrainingPlanGeneration(user);

        Optional<TrainingPlan> existingPlan =
                trainingPlanRepository.findByUserAndDateGenerated(user, LocalDate.now());
        if (existingPlan.isPresent()) {
            logger.info("Existing training plan found for user {}. Returning existing plan.", user.getFullName());
            TrainingPlan plan = existingPlan.get();

            Hibernate.initialize(plan.getTrainingSessions());
            if (plan.getTrainingSessions() != null) {
                plan.getTrainingSessions().forEach(session -> Hibernate.initialize(session.getExercises()));
            }

            Hibernate.initialize(plan.getUserActivityLevelSnapshot());
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
        if (user.getActivityLevel() != null) {
            Hibernate.initialize(user.getActivityLevel());
            trainingPlan.setUserActivityLevelSnapshot(user.getActivityLevel());
        }



        trainingPlan = trainingPlanRepository.save(trainingPlan);

        int daysPerWeek = trainingPlan.getDaysPerWeek();
        int durationMinutesPerSession = trainingPlan.getDurationMinutes();
        Random random = new Random();

        List<DayOfWeek> selectedDays = selectTrainingDays(daysPerWeek);

        for (DayOfWeek day : selectedDays) {
            TrainingSession session = TrainingSession.builder()
                    .dayOfWeek(day)
                    .durationMinutes(durationMinutesPerSession)
                    .trainingPlan(trainingPlan)
                    .build();

            List<Exercise> exercises = getExercisesForUser(userLevel, userTrainingType);
            if (exercises.isEmpty()) {
                logger.warn("No available exercises for {}. Skipping session.", day);
                continue;
            }

            Collections.shuffle(exercises);
            int count = Math.min(exercises.size(), 5 + random.nextInt(4));
            for (int i = 0; i < count; i++) {
                session.addExercise(exercises.get(i));
            }

            trainingSessionRepository.save(session);
            trainingPlan.addTrainingSession(session);
        }

        return trainingPlanRepository.save(trainingPlan);
    }

    private List<Exercise> getExercisesForUser(LevelType level, TrainingType type) {
        DifficultyLevel diff = getDifficultyLevelEnum(level);
        ExerciseType exType = getExerciseTypeEnum(type);

        List<Exercise> filtered = exerciseRepository.findByDifficultyLevelAndType(diff, exType);
        if (!filtered.isEmpty()) return filtered;

        List<Exercise> byType = exerciseRepository.findByType(exType);
        if (!byType.isEmpty()) return byType;

        return exerciseRepository.findAll();
    }

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
            throw new IllegalArgumentException("Попълнете липсващите полета: " + String.join(", ", missing));
        }
    }

    private List<DayOfWeek> selectTrainingDays(int days) {
        List<DayOfWeek> daysList = Arrays.asList(DayOfWeek.values());
        Collections.shuffle(daysList);
        return daysList.subList(0, Math.min(days, 7));
    }

    private int getDefaultTrainingDays(LevelType level) {
        return switch (level) {
            case BEGINNER -> 3;
            case INTERMEDIATE -> 4;
            case ADVANCED -> 5;
            default -> 3;
        };
    }

    private int getDefaultTrainingDuration(LevelType level) {
        return switch (level) {
            case BEGINNER -> 45;
            case INTERMEDIATE -> 60;
            case ADVANCED -> 75;
            default -> 60;
        };
    }

    private DifficultyLevel getDifficultyLevelEnum(LevelType level) {
        return switch (level) {
            case BEGINNER -> DifficultyLevel.BEGINNER;
            case INTERMEDIATE -> DifficultyLevel.INTERMEDIATE;
            case ADVANCED -> DifficultyLevel.ADVANCED;
        };
    }

    private ExerciseType getExerciseTypeEnum(TrainingType type) {
        return switch (type) {
            case WEIGHTS -> ExerciseType.WEIGHTS;
            case BODYWEIGHT -> ExerciseType.BODYWEIGHT;
            case CARDIO -> ExerciseType.CARDIO;
        };
    }

    public TrainingPlanDTO convertTrainingPlanToDTO(TrainingPlan plan) {
        if (plan == null) return null;
        Hibernate.initialize(plan.getUser());
        Hibernate.initialize(plan.getTrainingSessions());

        List<TrainingSessionDTO> sessionDTOs = plan.getTrainingSessions().stream()
                .map(this::convertSessionToDTO)
                .collect(Collectors.toList());

        return TrainingPlanDTO.builder()
                .id(plan.getId())
                .dateGenerated(plan.getDateGenerated())
                .daysPerWeek(plan.getDaysPerWeek())
                .durationMinutes(plan.getDurationMinutes())
                .userId(plan.getUser().getId())
                .userEmail(plan.getUser().getEmail())
                .trainingSessions(sessionDTOs)
                .build();
    }

    private TrainingSessionDTO convertSessionToDTO(TrainingSession session) {
        Hibernate.initialize(session.getExercises());

        List<ExerciseDTO> exerciseDTOs = session.getExercises().stream()
                .map(ex -> ExerciseDTO.builder()
                        .id(ex.getId())
                        .name(ex.getName())
                        .description(ex.getDescription())
                        .sets(ex.getSets())
                        .reps(ex.getReps())
                        .durationMinutes(ex.getDurationMinutes())
                        .type(ex.getType())
                        .difficultyLevel(ex.getDifficultyLevel())
                        .equipment(ex.getEquipment())
                        .build())
                .collect(Collectors.toList());

        return TrainingSessionDTO.builder()
                .id(session.getId())
                .dayOfWeek(session.getDayOfWeek())
                .durationMinutes(session.getDurationMinutes())
                .exercises(exerciseDTOs)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TrainingPlanDTO> getAllTrainingPlansDTO() {
        return trainingPlanRepository.findAll().stream()
                .map(this::convertTrainingPlanToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TrainingPlanDTO generateAndSaveTrainingPlanForUserDTO(User user) {
        TrainingPlan plan = generateAndSaveTrainingPlanForUser(user);
        return convertTrainingPlanToDTO(plan);
    }

    @Transactional(readOnly = true)
    public List<TrainingPlanDTO> getTrainingPlansByUserDTO(User user) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + user.getId() + " not found."));
        return trainingPlanRepository.findByUserOrderByDateGeneratedDesc(managedUser).stream()
                .map(this::convertTrainingPlanToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void fixMissingTrainingPlans() {
        logger.info("Fixing missing training plans...");
        userRepository.findAll().forEach(user -> {
            if (trainingPlanRepository.findByUserAndDateGenerated(user, LocalDate.now()).isEmpty()) {
                try {
                    TrainingPlan plan = generateAndSaveTrainingPlanForUser(user);
                    logger.info("Generated plan for user: {}", user.getEmail());
                } catch (Exception e) {
                    logger.error("Error for {}: {}", user.getEmail(), e.getMessage());
                }
            }
        });
    }

    @Transactional(readOnly = true)
    public TrainingPlanDTO getRecommendedTrainingPlanDTO(String goal, boolean withWeights) {
        logger.warn("Not implemented: getRecommendedTrainingPlanDTO");
        return null;
    }

    @Transactional(readOnly = true)
    public List<TrainingPlanHistoryDTO> getTrainingPlanHistory(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        List<TrainingPlan> plans = trainingPlanRepository.findByUserOrderByDateGeneratedDesc(user);

        return plans.stream()
                .map(plan -> {

                    Hibernate.initialize(plan.getUserActivityLevelSnapshot());

                    return TrainingPlanHistoryDTO.builder()
                            .id(plan.getId())
                            .dateGenerated(plan.getDateGenerated())
                            .trainingPlanDescription(plan.getTrainingPlanDescription())
                            .trainingDaysPerWeek(plan.getDaysPerWeek())
                            .trainingDurationMinutes(plan.getDurationMinutes())
                            .userGenderSnapshot(plan.getUserGenderSnapshot() != null ? plan.getUserGenderSnapshot().name() : null)
                            .userAgeSnapshot(plan.getUserAgeSnapshot())
                            .userWeightSnapshot(plan.getUserWeightSnapshot())
                            .userHeightSnapshot(plan.getUserHeightSnapshot())
                            .userActivityLevelSnapshotName(plan.getUserActivityLevelSnapshot() != null ? plan.getUserActivityLevelSnapshot().getName() : null)

                            .build();
                })
                .collect(Collectors.toList());
    }
}