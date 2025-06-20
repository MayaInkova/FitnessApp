package com.fitnessapp.controller;

import com.fitnessapp.dto.*;

import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.User;
import com.fitnessapp.service.NutritionPlanService;
import com.fitnessapp.service.TrainingPlanService;
import com.fitnessapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nutrition-plans")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8080"}) // По-специфични произходи за сигурност
public class NutritionPlanController {

    private final NutritionPlanService nutritionPlanService;
    private final TrainingPlanService trainingPlanService;
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(NutritionPlanController.class);

    @Autowired
    public NutritionPlanController(NutritionPlanService nutritionPlanService,
                                   TrainingPlanService trainingPlanService,
                                   UserService userService) {
        this.nutritionPlanService = nutritionPlanService;
        this.trainingPlanService = trainingPlanService;
        this.userService = userService;
    }

    // ----------- ЕНДПОЙНТИ ЗА ГЕНЕРИРАНЕ НА ПЛАНОВЕ -----------

    /**
     * Генерира и запазва ДНЕВЕН хранителен план и тренировъчен план за потребител.
     * Отговаря на POST /api/nutrition-plans/generate-daily/{userId}
     */
    @PostMapping("/generate-daily/{userId}")
    public ResponseEntity<?> generateDailyPlan(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("type", "error", "message", "Потребител с ID " + userId + " не е намерен."));
            }

            NutritionPlanDTO nutritionPlanDTO = nutritionPlanService.generateAndSaveNutritionPlanForUserDTO(user);
            TrainingPlanDTO trainingPlanDTO = trainingPlanService.generateAndSaveTrainingPlanForUserDTO(user);

            return ResponseEntity.ok(new PlanBundleResponse(nutritionPlanDTO, trainingPlanDTO));
        } catch (IllegalArgumentException e) {
            logger.warn("Липсващи потребителски данни за генериране на дневен план: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("type", "error", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Грешка при генериране на дневен план за потребител {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("type", "error", "message", "Възникна вътрешна грешка при генериране на дневен план: " + e.getMessage()));
        }
    }

    /**
     * Генерира и запазва СЕДМИЧЕН хранителен план за потребител.
     * Отговаря на POST /api/nutrition-plans/generate-weekly/{userId}
     */
    @PostMapping("/generate-weekly/{userId}") // НОВ ЕНДПОЙНТ ЗА СЕДМИЧЕН ПЛАН
    public ResponseEntity<?> generateWeeklyNutritionPlan(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("type", "error", "message", "Потребител с ID " + userId + " не е намерен."));
            }

            // Извикваме новия метод за генериране на седмичен план
            WeeklyNutritionPlanDTO weeklyPlanDTO = nutritionPlanService.generateAndSaveWeeklyNutritionPlanForUserDTO(user);

            return ResponseEntity.ok(weeklyPlanDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Липсващи потребителски данни за генериране на седмичен хранителен план: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("type", "error", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Грешка при генериране на седмичен хранителен план за потребител {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("type", "error", "message", "Възникна вътрешна грешка при генериране на седмичен хранителен план: " + e.getMessage()));
        }
    }


    // ----------- ЕНДПОЙНТИ ЗА ИЗВЛИЧАНЕ НА ПЛАНОВЕ -----------

    /**
     * Извлича последния ДНЕВЕН хранителен план за потребител.
     * Отговаря на GET /api/nutrition-plans/latest-daily/{userId}
     */
    @GetMapping("/latest-daily/{userId}") // Преименувах, за да е по-ясно, че е за дневен
    public ResponseEntity<?> getLatestDailyNutritionPlanByUserId(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("type", "error", "message", "Потребител с ID " + userId + " не е намерен."));
            }

            List<NutritionPlanDTO> plansDTO = nutritionPlanService.getNutritionPlansByUserDTO(user);
            NutritionPlanDTO latestPlanDTO = plansDTO.stream().findFirst().orElse(null);

            return latestPlanDTO != null ? ResponseEntity.ok(latestPlanDTO)
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("type", "error", "message", "Няма намерен дневен хранителен план за потребител с ID " + userId));
        } catch (Exception e) {
            logger.error("Грешка при извличане на дневен план по потребител {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("type", "error", "message", "Възникна грешка при извличане на дневен план: " + e.getMessage()));
        }
    }

    /**
     * Извлича историята на всички ДНЕВНИ хранителни планове за потребител.
     * Отговаря на GET /api/nutrition-plans/history/{userId}
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getNutritionPlanHistory(@PathVariable Integer userId) { // Преименувах метода за яснота
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("type", "error", "message", "Потребител с ID " + userId + " не е намерен."));
            }

            // Извикваме nutritionPlanService.getNutritionPlanHistory, който връща NutritionPlanHistoryDTO
            List<NutritionPlanHistoryDTO> plansHistoryDTO = nutritionPlanService.getNutritionPlanHistory(userId);
            return ResponseEntity.ok(plansHistoryDTO);
        } catch (Exception e) {
            logger.error("Грешка при зареждане на историята на хранителните планове за потребител {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("type", "error", "message", "Грешка при зареждане на историята: " + e.getMessage()));
        }
    }


    /**
     * Извлича комбиниран (хранителен и тренировъчен) план за потребител.
     * Отговаря на GET /api/nutrition-plans/full?userId={userId}
     */
    @GetMapping("/full")
    public ResponseEntity<?> getFullPlan(@RequestParam Integer userId) {
        try {
            logger.info("В getFullPlan: userId = {}, Тип = {}", userId, userId.getClass().getName());

            FullPlanDTO fullPlan = nutritionPlanService.getFullPlanByUserId(userId);
            if (fullPlan == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("type", "error", "message", "Няма намерен план за потребителя."));
            }
            return ResponseEntity.ok(fullPlan);
        } catch (ClassCastException e) {
            logger.error("Грешка при преобразуване на тип за потребителско ID в getFullPlan: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("type", "error", "message", "Невалиден формат на потребителско ID."));
        } catch (Exception e) {
            logger.error("Грешка при извличане на пълен план за потребител {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("type", "error", "message", "Вътрешна грешка при извличане на пълен план: " + e.getMessage()));
        }
    }


    // ----------- АДМИНСКИ И ДР. ЕНДПОЙНТИ -----------

    /**
     * Записва нов NutritionPlan entity (само за ADMIN).
     * Отговаря на POST /api/nutrition-plans
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createNutritionPlan(@RequestBody NutritionPlan plan) {
        try {
            NutritionPlanDTO savedPlanDTO = nutritionPlanService.saveNutritionPlan(plan);
            return ResponseEntity.ok(savedPlanDTO);
        } catch (Exception e) {
            logger.error("Грешка при създаване на хранителен план: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("type", "error", "message", "Грешка при създаване на план: " + e.getMessage()));
        }
    }

    /**
     * Извлича всички хранителни планове (само за ADMIN).
     * Отговаря на GET /api/nutrition-plans/all
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllNutritionPlans() {
        try {
            List<NutritionPlanDTO> plansDTO = nutritionPlanService.getAllNutritionPlansDTO();
            return ResponseEntity.ok(plansDTO);
        } catch (Exception e) {
            logger.error("Грешка при зареждане на всички планове: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("type", "error", "message", "Грешка при зареждане: " + e.getMessage()));
        }
    }

    /**
     * Дебъг ендпойнт за извличане на последния дневен план (само за ADMIN).
     * Отговаря на GET /api/nutrition-plans/debug/{userId}
     */
    @GetMapping("/debug/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> debugPlan(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("type", "error", "message", "Потребител с ID " + userId + " не е намерен."));
            }

            List<NutritionPlanDTO> plansDTO = nutritionPlanService.getNutritionPlansByUserDTO(user);
            NutritionPlanDTO latestPlanDTO = plansDTO.stream().findFirst().orElse(null);

            return ResponseEntity.ok(latestPlanDTO);
        } catch (Exception e) {
            logger.error("Грешка при отстраняване на грешки в план за потребител {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("type", "error", "message", "Грешка при отстраняване на грешки: " + e.getMessage()));
        }
    }

    /**
     * Коригира липсващи тренировъчни планове (само за ADMIN).
     * Отговаря на GET /api/nutrition-plans/fix-training
     */
    @GetMapping("/fix-training")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> fixTrainingPlans() {
        try {
            trainingPlanService.fixMissingTrainingPlans();
            return ResponseEntity.ok(Map.of("type", "success", "message", "Липсващите тренировъчни планове са успешно генерирани."));
        } catch (Exception e) {
            logger.error("Грешка при коригиране на липсващи тренировъчни планове: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("type", "error", "message", "Грешка при коригиране: " + e.getMessage()));
        }
    }
}