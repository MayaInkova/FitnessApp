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

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nutrition-plans")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8080"})
public class NutritionPlanController {

    private final NutritionPlanService nutritionPlanService;
    private final TrainingPlanService  trainingPlanService;
    private final UserService          userService;

    private static final Logger logger = LoggerFactory.getLogger(NutritionPlanController.class);

    @Autowired
    public NutritionPlanController(NutritionPlanService nutritionPlanService,
                                   TrainingPlanService trainingPlanService,
                                   UserService userService) {
        this.nutritionPlanService = nutritionPlanService;
        this.trainingPlanService  = trainingPlanService;
        this.userService          = userService;
    }


    @PostMapping("/generate-daily/{userId}")
    public ResponseEntity<?> generateDailyPlan(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("type", "error",
                                "message", "Потребител с ID " + userId + " не е намерен."));
            }

            NutritionPlanDTO nutritionPlanDTO = nutritionPlanService.generateAndSaveNutritionPlanForUserDTO(user);
            TrainingPlanDTO  trainingPlanDTO  = trainingPlanService.generateAndSaveTrainingPlanForUserDTO(user);

            return ResponseEntity.ok(new PlanBundleResponse(nutritionPlanDTO, trainingPlanDTO));

        } catch (IllegalArgumentException e) {
            logger.warn("Липсващи потребителски данни: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("type", "error", "message", e.getMessage()));

        } catch (Exception e) {
            logger.error("Грешка при генериране на дневен план за {}: ", userId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("type", "error", "message",
                            "Възникна вътрешна грешка: " + e.getMessage()));
        }
    }


    @GetMapping("/weekly/{userId}")
    public ResponseEntity<?> getOrCreateWeeklyNutritionPlan(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("type", "error",
                                "message", "Потребител с ID " + userId + " не е намерен."));
            }

            WeeklyNutritionPlanDTO weeklyPlanDTO = nutritionPlanService.getOrCreateWeeklyPlan(user);
            return ResponseEntity.ok(weeklyPlanDTO);

        } catch (Exception e) {
            logger.error("Грешка при извличане/създаване на седмичен план за {}: ", userId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("type", "error",
                            "message", "Грешка при обработка на заявката: " + e.getMessage()));
        }
    }


    @GetMapping("/latest-daily/{userId}")
    public ResponseEntity<?> getLatestDailyNutritionPlanByUserId(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("type", "error",
                                "message", "Потребител с ID " + userId + " не е намерен."));
            }

            List<NutritionPlanDTO> plans = nutritionPlanService.getNutritionPlansByUserDTO(user);
            NutritionPlanDTO latest = plans.stream().findFirst().orElse(null);

            if (latest == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("type", "error",
                                "message", "Няма дневен план за този потребител."));
            }

            return ResponseEntity.ok(latest);

        } catch (Exception e) {
            logger.error("Грешка при извличане на дневен план: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("type", "error",
                            "message", "Грешка при извличане: " + e.getMessage()));
        }
    }


    @PutMapping("/{planId}/replace-meal")
    public WeeklyNutritionPlanDTO replaceMeal(@PathVariable Integer planId,
                                              @RequestBody ReplaceMealRequest req) {
        return nutritionPlanService.replaceMeal(planId,
                req.originalMealId(), req.substituteRecipeId());
    }


    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getNutritionPlanHistory(@PathVariable Integer userId) {
        try {
            List<NutritionPlanHistoryDTO> history =
                    nutritionPlanService.getNutritionPlanHistory(userId);
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            logger.error("Грешка при зареждане на историята: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("type", "error",
                            "message", "Грешка при зареждане: " + e.getMessage()));
        }
    }


    @GetMapping("/full")
    public ResponseEntity<?> getFullPlan(@RequestParam Integer userId) {
        try {
            FullPlanDTO fullPlan = nutritionPlanService.getFullPlanByUserId(userId);
            if (fullPlan == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("type", "error", "message", "Няма намерен план за потребителя."));
            }
            return ResponseEntity.ok(fullPlan);

        } catch (Exception e) {
            logger.error("Грешка при извличане на пълен план: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("type", "error",
                            "message", "Вътрешна грешка: " + e.getMessage()));
        }
    }



    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createNutritionPlan(@RequestBody NutritionPlan plan) {
        try {
            NutritionPlanDTO saved = nutritionPlanService.saveNutritionPlan(plan);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            logger.error("Грешка при създаване на хранителен план: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("type", "error", "message", "Грешка при създаване: " + e.getMessage()));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllNutritionPlans() {
        try {
            return ResponseEntity.ok(nutritionPlanService.getAllNutritionPlansDTO());

        } catch (Exception e) {
            logger.error("Грешка при зареждане на всички планове: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("type", "error", "message", "Грешка при зареждане: " + e.getMessage()));
        }
    }

    @GetMapping("/debug/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> debugPlan(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("type", "error",
                                "message", "Потребител с ID " + userId + " не е намерен."));
            }
            NutritionPlanDTO latest =
                    nutritionPlanService.getNutritionPlansByUserDTO(user).stream()
                            .findFirst().orElse(null);
            return ResponseEntity.ok(latest);

        } catch (Exception e) {
            logger.error("Debug грешка: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("type", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/fix-training")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> fixTrainingPlans() {
        try {
            trainingPlanService.fixMissingTrainingPlans();
            return ResponseEntity.ok(
                    Map.of("type", "success",
                            "message", "Липсващите тренировъчни планове са генерирани."));
        } catch (Exception e) {
            logger.error("Грешка при fix-training: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("type", "error", "message", e.getMessage()));
        }
    }
}