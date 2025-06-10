package com.fitnessapp.controller;

import com.fitnessapp.dto.FullPlanDTO;
import com.fitnessapp.dto.NutritionPlanDTO;
import com.fitnessapp.dto.PlanBundleResponse;
import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.TrainingPlan;
import com.fitnessapp.model.User;
import com.fitnessapp.service.NutritionPlanService;
import com.fitnessapp.service.TrainingPlanService; // <-- Важно: Този импорт е необходим!
import com.fitnessapp.service.UserService;
import jakarta.transaction.Transactional; // Не е нужен за контролера, но не пречи
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nutrition-plans")
@CrossOrigin(origins = "*")
public class NutritionPlanController {

    private final NutritionPlanService nutritionPlanService;
    private final TrainingPlanService trainingPlanService; // Инжектираме TrainingPlanService
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(NutritionPlanController.class);

    @Autowired
    public NutritionPlanController(NutritionPlanService nutritionPlanService,
                                   TrainingPlanService trainingPlanService, // Добавяме го към конструктора
                                   UserService userService) {
        this.nutritionPlanService = nutritionPlanService;
        this.trainingPlanService = trainingPlanService; // Инициализираме го
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> createNutritionPlan(@RequestBody NutritionPlan plan) {
        try {
            NutritionPlan savedPlan = nutritionPlanService.savePlan(plan);
            return ResponseEntity.ok(savedPlan);
        } catch (Exception e) {
            logger.error("Грешка при създаване на хранителен план: ", e);
            return ResponseEntity.status(500).body("Грешка при създаване на план: " + e.getMessage());
        }
    }

    @PostMapping("/generate/{userId}")
    public ResponseEntity<?> generatePlan(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) return ResponseEntity.notFound().build();

            // Проверка за DietType:
            // Ако DietType е Lazy Loaded и не е зареден, може да хвърли LazyInitializationException.
            // Уверете се, че getDietType() зарежда DietType или го заредете предварително.
            // В generatePlanForUser може да се подаде и директно User обект, а не само името на диетата.
            String dietTypeName = user.getDietType() != null ? user.getDietType().getName() : null;
            NutritionPlan nutritionPlan = nutritionPlanService.generatePlanForUser(user, dietTypeName);

            TrainingPlan trainingPlan = nutritionPlan.getTrainingPlan();

            return ResponseEntity.ok(new PlanBundleResponse(nutritionPlan, trainingPlan));
        } catch (Exception e) {
            logger.error("Грешка при генериране на план: ", e);
            return ResponseEntity.status(500).body("Грешка при генериране: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getNutritionPlanByUserId(@PathVariable Integer userId) {
        try {
            NutritionPlan plan = nutritionPlanService.getPlanByUserId(userId);
            return plan != null ? ResponseEntity.ok(plan) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Грешка при вземане на план по потребител: ", e);
            return ResponseEntity.status(500).body("Възникна грешка: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllNutritionPlans() {
        try {
            List<NutritionPlan> plans = nutritionPlanService.getAllPlans();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            logger.error("Грешка при зареждане на всички планове: ", e);
            return ResponseEntity.status(500).body("Грешка при зареждане: " + e.getMessage());
        }
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getPlanHistory(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId); // Уверете се, че user е зареден коректно
            if (user == null) {
                return ResponseEntity.status(403).body("Достъпът е разрешен само за регистрирани потребители.");
            }

            List<NutritionPlan> plans = nutritionPlanService.getAllNutritionPlansForUser(userId);

            List<NutritionPlanDTO> dtoList = plans.stream()
                    .map(p -> new NutritionPlanDTO(
                            p.getId(),
                            p.getCalories(),
                            p.getProtein(),
                            p.getFat(),
                            p.getCarbs(),
                            p.getGoal()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);
        } catch (Exception e) {
            logger.error("Грешка при зареждане на историята на плановете: ", e);
            return ResponseEntity.status(500).body("Грешка при зареждане на историята");
        }
    }

    @GetMapping("/weekly/{userId}")
    public ResponseEntity<?> generateWeeklyPlan(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(403).body("Само за регистрирани потребители.");
            }

            String dietTypeName = user.getDietType() != null ? user.getDietType().getName() : null;
            NutritionPlan nutritionPlan = nutritionPlanService.generatePlanForUser(user, dietTypeName);

            TrainingPlan trainingPlan = nutritionPlan.getTrainingPlan();

            return ResponseEntity.ok(
                    new PlanBundleResponse(nutritionPlan, trainingPlan)
            );
        } catch (Exception e) {
            logger.error("Грешка при генериране на седмичен план: ", e);
            return ResponseEntity.status(500).body("Грешка при седмичен режим: " + e.getMessage());
        }
    }

    @GetMapping("/full")
    @Transactional // Тази транзакция може да е в Service слоя, но не пречи тук
    public ResponseEntity<?> getFullPlan(@RequestParam Integer userId) {
        try {
            FullPlanDTO fullPlan = nutritionPlanService.getFullPlanByUserId(userId);
            if (fullPlan == null) {
                return ResponseEntity.status(404).body("Няма намерен план за потребителя.");
            }
            return ResponseEntity.ok(fullPlan);
        } catch (Exception e) {
            logger.error("Грешка при вземане на пълен план: ", e);
            return ResponseEntity.status(500).body("Вътрешна грешка: " + e.getMessage());
        }
    }

    @GetMapping("/debug/{userId}")
    public ResponseEntity<?> debugPlan(@PathVariable Integer userId) {
        var plan = nutritionPlanService.getPlanByUserId(userId);
        return ResponseEntity.ok(plan);
    }

    // Правилно извикване на fixMissingTrainingPlans от trainingPlanService
    @GetMapping("/fix-training") // Използвам GET за удобство при тестване, но POST би бил по-подходящ за променящи операции
    public ResponseEntity<?> fixTrainingPlans() {
        try {
            trainingPlanService.fixMissingTrainingPlans(); // <-- КОРИГИРАНО ИЗВИКВАНЕ
            return ResponseEntity.ok("Липсващите тренировъчни планове са успешно генерирани/актуализирани.");
        } catch (Exception e) {
            logger.error("Грешка при фиксиране на липсващи тренировъчни планове: ", e);
            return ResponseEntity.status(500).body("Възникна грешка при фиксиране на липсващи тренировъчни планове: " + e.getMessage());
        }
    }
}