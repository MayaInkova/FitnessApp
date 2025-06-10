package com.fitnessapp.service;

import com.fitnessapp.model.Exercise;
import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.TrainingPlan;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.ExerciseRepository;
import com.fitnessapp.repository.NutritionPlanRepository;
import com.fitnessapp.repository.TrainingPlanRepository;
import com.fitnessapp.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final NutritionPlanRepository nutritionPlanRepository;

    @Autowired
    public TrainingPlanService(TrainingPlanRepository trainingPlanRepository,
                               ExerciseRepository exerciseRepository,
                               UserRepository userRepository,
                               NutritionPlanRepository nutritionPlanRepository) {
        this.trainingPlanRepository = trainingPlanRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
        this.nutritionPlanRepository = nutritionPlanRepository;
    }

    public List<Exercise> getExercisesForPlan(Integer trainingPlanId) {
        return exerciseRepository.findByTrainingPlanId(trainingPlanId);
    }

    public List<TrainingPlan> getAll() {
        return trainingPlanRepository.findAll();
    }

    public TrainingPlan save(TrainingPlan plan) {
        return trainingPlanRepository.save(plan);
    }

    public TrainingPlan getRecommended(String goal, boolean withWeights) {
        List<TrainingPlan> suitableTemplates = trainingPlanRepository
                .findByGoalAndWithWeights(goal, withWeights);

        if (suitableTemplates.isEmpty()) {
            suitableTemplates = trainingPlanRepository.findByGoal(goal);
            if (suitableTemplates.isEmpty()) {
                suitableTemplates = trainingPlanRepository.findByWithWeights(withWeights);
                if (suitableTemplates.isEmpty()) {
                    // Fallback to any existing plan if no specific matches
                    if (trainingPlanRepository.count() > 0) {
                        return trainingPlanRepository.findAll().get(new Random().nextInt((int) trainingPlanRepository.count()));
                    } else {
                        throw new IllegalStateException("Няма налични тренировъчни шаблони в базата данни.");
                    }
                }
            }
        }

        Random random = new Random();
        return suitableTemplates.get(random.nextInt(suitableTemplates.size()));
    }

    @Transactional
    public TrainingPlan generateTrainingPlanForUser(User user) {
        String goal = user.getGoal();
        boolean userWantsWeights = user.getTrainingType() != null && user.getTrainingType().toLowerCase().contains("тежест");
        String userLevel = user.getLevel() != null ? user.getLevel().toLowerCase() : "beginner";

        List<TrainingPlan> suitableTemplates = trainingPlanRepository
                .findByGoalAndWithWeights(goal, userWantsWeights);

        if (suitableTemplates.isEmpty()) {
            suitableTemplates = trainingPlanRepository.findByGoal(goal);
            if (suitableTemplates.isEmpty()) {
                suitableTemplates = trainingPlanRepository.findByWithWeights(userWantsWeights);
                if (suitableTemplates.isEmpty() && trainingPlanRepository.count() > 0) {
                    return trainingPlanRepository.findAll().get(0);
                } else if (suitableTemplates.isEmpty()){
                    throw new IllegalStateException("Няма налични тренировъчни шаблони в базата данни.");
                }
            }
        }

        Random random = new Random();
        TrainingPlan baseTemplate = suitableTemplates.get(random.nextInt(suitableTemplates.size()));

        TrainingPlan userTrainingPlan = new TrainingPlan();
        userTrainingPlan.setName(baseTemplate.getName() + " за " + user.getFullName());
        userTrainingPlan.setDescription(baseTemplate.getDescription());
        userTrainingPlan.setGoal(baseTemplate.getGoal());
        userTrainingPlan.setLevel(baseTemplate.getLevel());
        userTrainingPlan.setWithWeights(baseTemplate.isWithWeights());
        userTrainingPlan.setDaysPerWeek(baseTemplate.getDaysPerWeek());
        userTrainingPlan.setDurationMinutes(baseTemplate.getDurationMinutes());
        userTrainingPlan.setExercises(new ArrayList<>());

        List<Exercise> allAvailableExercises = exerciseRepository.findAll();

        List<String> days = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        int exercisesPerTrainingDay = 4;

        for (int i = 0; i < baseTemplate.getDaysPerWeek(); i++) {
            String currentDay = days.get(i);

            List<Exercise> suitableExercises = allAvailableExercises.stream()
                    .filter(ex -> ex.isRequiresWeights() == userWantsWeights)
                    .filter(ex -> "all".equalsIgnoreCase(ex.getLevel()) || userLevel.equalsIgnoreCase(ex.getLevel()))
                    .collect(Collectors.toList());

            if (suitableExercises.isEmpty()) {
                System.err.println("ВНИМАНИЕ: Няма достатъчно подходящи упражнения за генериране на тренировъчен план с цел: " + goal + ", тежести: " + userWantsWeights + ", ниво: " + userLevel);
                break;
            }

            Collections.shuffle(suitableExercises, random);

            List<Exercise> dailyExercises = new ArrayList<>();
            for (int j = 0; j < Math.min(exercisesPerTrainingDay, suitableExercises.size()); j++) {
                Exercise selectedExercise = suitableExercises.get(j);

                Exercise exerciseForPlan = Exercise.builder()
                        .name(selectedExercise.getName())
                        .description(selectedExercise.getDescription())
                        .sets(selectedExercise.getSets())
                        .reps(selectedExercise.getReps())
                        .restSeconds(selectedExercise.getRestSeconds())
                        .videoUrl(selectedExercise.getVideoUrl())
                        .imageUrl(selectedExercise.getImageUrl())
                        .isRequiresWeights(selectedExercise.isRequiresWeights())
                        .bodyPart(selectedExercise.getBodyPart()) // <-- Копирайте от шаблона
                        .equipment(selectedExercise.getEquipment()) // <-- Копирайте от шаблона
                        .exerciseType(selectedExercise.getExerciseType()) // <-- Копирайте от шаблона
                        .level(selectedExercise.getLevel())
                        .dayOfWeek(currentDay)
                        .trainingPlan(userTrainingPlan)
                        .build();
                dailyExercises.add(exerciseForPlan);
            }
            userTrainingPlan.getExercises().addAll(dailyExercises);
        }
        return trainingPlanRepository.save(userTrainingPlan);
    }

    @Transactional
    public int fixMissingTrainingPlans() {
        List<User> allUsers = userRepository.findAll();
        int fixedCount = 0;

        if (allUsers.isEmpty()) {
            System.out.println("Няма регистрирани потребители.");
            return 0;
        }

        System.out.println("Проверявам за липсващи тренировъчни планове за " + allUsers.size() + " потребители. Започва генериране...");

        for (User user : allUsers) {
            try {
                NutritionPlan latestNutritionPlan = nutritionPlanRepository.findTopByUserIdOrderByIdDesc(user.getId());

                TrainingPlan existingTrainingPlan = null;
                if (latestNutritionPlan != null) {
                    existingTrainingPlan = latestNutritionPlan.getTrainingPlan();
                }

                if (existingTrainingPlan == null) {
                    System.out.println("-> Генерирам нов тренировъчен план за потребител: " + user.getFullName() + " (ID: " + user.getId() + ")");

                    if (latestNutritionPlan == null) {
                        System.out.println("   (Потребител " + user.getFullName() + " няма хранителен план. Пропускам генериране на тренировъчен план.)");
                        continue;
                    } else {
                        TrainingPlan generatedPlan = generateTrainingPlanForUser(user);
                        latestNutritionPlan.setTrainingPlan(generatedPlan);
                        nutritionPlanRepository.save(latestNutritionPlan);
                        System.out.println("   -> Свързан нов тренировъчен план с последния хранителен план на потребителя.");
                        fixedCount++;
                    }

                } else {
                    System.out.println("-> Потребител: " + user.getFullName() + " (ID: " + user.getId() + ") вече има тренировъчен план.");
                }
            } catch (Exception e) {
                System.err.println("-> Грешка при генериране на тренировъчен план за потребител " + user.getFullName() + " (ID: " + user.getId() + "): " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("Процесът по генериране на липсващи тренировъчни планове приключи. Генерирани/актуализирани: " + fixedCount);
        return fixedCount;
    }


    @Transactional
    public void seedDefaultTrainingPlans() {
        // Проверяваме дали вече има упражнения, преди да ги добавим отново
        if (exerciseRepository.count() > 0) {
            System.out.println("Вече има упражнения в базата данни. Пропускам 'seed' за упражнения.");
            // Може да изтриете старите данни ако искате да ги "презапишете"
            // exerciseRepository.deleteAll();
            // trainingPlanRepository.deleteAll(); // Изтрийте и плановете, ако презаписвате
            // return; // Ако не искате да продължавате след проверка
        }

        // Първо запазваме упражненията, които ще се използват в плановете
        List<Exercise> defaultExercises = List.of(
                Exercise.builder().name("Ходене на място").description("Леко кардио за загрявка.").sets(1).reps(120).restSeconds(0).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/walking.jpg").isRequiresWeights(false).bodyPart("Full Body").equipment("Bodyweight").exerciseType("Cardio").level("beginner").build(),
                Exercise.builder().name("Скачане на въже").description("Високоинтензивно кардио.").sets(3).reps(60).restSeconds(30).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/jumprope.jpg").isRequiresWeights(false).bodyPart("Full Body").equipment("Bodyweight").exerciseType("Cardio").level("intermediate").build(),
                Exercise.builder().name("Йога - Поздрав към слънцето").description("Подобрява гъвкавостта и мобилността.").sets(1).reps(5).restSeconds(0).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/yoga.jpg").isRequiresWeights(false).bodyPart("Full Body").equipment("Bodyweight").exerciseType("Flexibility").level("beginner").build(),
                Exercise.builder().name("Лицеви опори").description("Упражнение за гърди и трицепс.").sets(3).reps(10).restSeconds(60).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/pushup.jpg").isRequiresWeights(false).bodyPart("Chest").equipment("Bodyweight").exerciseType("Strength").level("beginner").build(),
                Exercise.builder().name("Клек със собствена тежест").description("Основни упражнения за крака и глутеус.").sets(3).reps(15).restSeconds(60).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/squat.jpg").isRequiresWeights(false).bodyPart("Legs").equipment("Bodyweight").exerciseType("Strength").level("beginner").build(),
                Exercise.builder().name("Планк").description("Укрепва коремните мускули.").sets(3).reps(60).restSeconds(30).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/plank.jpg").isRequiresWeights(false).bodyPart("Core").equipment("Bodyweight").exerciseType("Strength").level("beginner").build(),
                Exercise.builder().name("Бърпи").description("Кардио и силово упражнение за цяло тяло.").sets(3).reps(8).restSeconds(60).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/burpee.jpg").isRequiresWeights(false).bodyPart("Full Body").equipment("Bodyweight").exerciseType("Plyometrics").level("intermediate").build(),

                // Упражнения с тежести
                Exercise.builder().name("Клек с щанга").description("Базово упражнение за крака.").sets(4).reps(8).restSeconds(90).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/barbellsquat.jpg").isRequiresWeights(true).bodyPart("Legs").equipment("Barbell").exerciseType("Strength").level("intermediate").build(),
                Exercise.builder().name("Изтласкване от лег (Bench Press)").description("Упражнение за гърди.").sets(4).reps(8).restSeconds(90).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/benchpress.jpg").isRequiresWeights(true).bodyPart("Chest").equipment("Barbell").exerciseType("Strength").level("intermediate").build(),
                Exercise.builder().name("Мъртва тяга").description("Комплексно упражнение за цялото тяло.").sets(3).reps(6).restSeconds(120).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/deadlift.jpg").isRequiresWeights(true).bodyPart("Full Body").equipment("Barbell").exerciseType("Strength").level("advanced").build(),
                Exercise.builder().name("Гребане с дъмбели").description("Упражнение за гръб.").sets(3).reps(10).restSeconds(60).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/dumbbellrow.jpg").isRequiresWeights(true).bodyPart("Back").equipment("Dumbbell").exerciseType("Strength").level("intermediate").build(),
                Exercise.builder().name("Раменни преси с дъмбели").description("Упражнение за рамене.").sets(3).reps(10).restSeconds(60).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/shoulderpress.jpg").isRequiresWeights(true).bodyPart("Shoulders").equipment("Dumbbell").exerciseType("Strength").level("intermediate").build(),
                Exercise.builder().name("Бицепсово сгъване с щанга").description("Упражнение за бицепс.").sets(3).reps(12).restSeconds(45).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/barbellcurl.jpg").isRequiresWeights(true).bodyPart("Arms").equipment("Barbell").exerciseType("Strength").level("beginner").build(),
                Exercise.builder().name("Трицепсово разгъване на скрипец").description("Упражнение за трицепс.").sets(3).reps(12).restSeconds(45).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/tricepextension.jpg").isRequiresWeights(true).bodyPart("Arms").equipment("Machine").exerciseType("Strength").level("beginner").build(),
                Exercise.builder().name("Напади с дъмбели").description("Упражнение за крака и глутеус.").sets(3).reps(10).restSeconds(60).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/dumbbelllunge.jpg").isRequiresWeights(true).bodyPart("Legs").equipment("Dumbbell").exerciseType("Strength").level("intermediate").build(),
                Exercise.builder().name("Разтягане на подколенни сухожилия").description("Разтягане за гъвкавост.").sets(3).reps(30).restSeconds(0).videoUrl("https://www.youtube.com/embed/videoseries?list=PL2_j_9aT5f-wF_G_o_x_y_z_a_b_c_d").imageUrl("https://example.com/hamstringstretch.jpg").isRequiresWeights(false).bodyPart("Legs").equipment("Bodyweight").exerciseType("Flexibility").level("all").build()
        );
        exerciseRepository.saveAll(defaultExercises); // Запазете всички упражнения

        // След като упражненията са запазени, можете да ги използвате за планове
        if (trainingPlanRepository.count() > 0) {
            System.out.println("Вече има тренировъчни планове в базата данни. Пропускам 'seed' за тренировъчни планове.");
            return;
        }

        TrainingPlan cardioTemplate = TrainingPlan.builder()
                .name("Кардио и мобилност")
                .goal("weight_loss")
                .level("beginner")
                .withWeights(false)
                .durationMinutes(30)
                .daysPerWeek(5)
                .description("План без тежести, фокусиран върху движение и здравословна активност. Подходящ за начинаещи.")
                .build();
        // Присвоете упражнения към плана, ако е необходимо
        // Пример:
        // List<Exercise> exercisesForCardio = defaultExercises.stream()
        //         .filter(e -> !e.isRequiresWeights() && e.getExerciseType().equals("Cardio"))
        //         .collect(Collectors.toList());
        // cardioTemplate.setExercises(exercisesForCardio);
        trainingPlanRepository.save(cardioTemplate);


        TrainingPlan weightsTemplate = TrainingPlan.builder()
                .name("Сила и мускулна маса")
                .goal("muscle_gain")
                .level("intermediate")
                .withWeights(true)
                .durationMinutes(45)
                .daysPerWeek(4)
                .description("План с тежести за напреднали, с фокус върху мускулна хипертрофия.")
                .build();

        // Пример:
        // List<Exercise> exercisesForWeights = defaultExercises.stream()
        //         .filter(Exercise::isRequiresWeights)
        //         .collect(Collectors.toList());
        // weightsTemplate.setExercises(exercisesForWeights);
        trainingPlanRepository.save(weightsTemplate);
    }
}