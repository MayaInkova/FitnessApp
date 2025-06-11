package com.fitnessapp.controller;

import com.fitnessapp.dto.FullPlanDTO;
import com.fitnessapp.dto.PlanBundleResponse;
import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.TrainingPlan;
import com.fitnessapp.model.User;
import com.fitnessapp.service.NutritionPlanService;
import com.fitnessapp.service.TrainingPlanService;
import com.fitnessapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Likely needed for admin endpoints
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/nutrition-plans")
@CrossOrigin(origins = "*")
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

    @PostMapping
    public ResponseEntity<?> createNutritionPlan(@RequestBody NutritionPlan plan) {
        try {
            NutritionPlan savedPlan = nutritionPlanService.saveNutritionPlan(plan);
            return ResponseEntity.ok(savedPlan);
        } catch (Exception e) {
            logger.error("Грешка при създаване на хранителен план: ", e);
            return ResponseEntity.status(500).body("Грешка при създаване на план: " + e.getMessage());
        }
    }

    @PostMapping("/generate/{userId}")
    // @PreAuthorize("isAuthenticated() and (#userId == authentication.principal.id or hasRole('ADMIN'))") // Example security
    public ResponseEntity<?> generatePlan(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) return ResponseEntity.notFound().build();

            NutritionPlan nutritionPlan = nutritionPlanService.generateNutritionPlan(user);
            TrainingPlan trainingPlan = trainingPlanService.generateAndSaveTrainingPlanForUser(user);

            return ResponseEntity.ok(new PlanBundleResponse(nutritionPlan, trainingPlan));
        } catch (Exception e) {
            logger.error("Грешка при генериране на план: ", e);
            return ResponseEntity.status(500).body("Грешка при генериране: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    // @PreAuthorize("isAuthenticated() and (#userId == authentication.principal.id or hasRole('ADMIN'))") // Example security
    public ResponseEntity<?> getNutritionPlanByUserId(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) return ResponseEntity.notFound().build();

            List<NutritionPlan> plans = nutritionPlanService.getNutritionPlansByUser(user);
            NutritionPlan latestPlan = plans.stream().findFirst().orElse(null); // This assumes findFirst() is the latest, consider sorting by date if not.

            return latestPlan != null ? ResponseEntity.ok(latestPlan) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Грешка при вземане на план по потребител: ", e);
            return ResponseEntity.status(500).body("Възникна грешка: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')") // This endpoint should likely be admin-only
    public ResponseEntity<?> getAllNutritionPlans() {
        try {
            List<NutritionPlan> plans = nutritionPlanService.getAllNutritionPlans();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            logger.error("Грешка при зареждане на всички планове: ", e);
            return ResponseEntity.status(500).body("Грешка при зареждане: " + e.getMessage());
        }
    }

    @GetMapping("/history/{userId}")
    // @PreAuthorize("isAuthenticated() and (#userId == authentication.principal.id or hasRole('ADMIN'))") // Example security
    public ResponseEntity<?> getPlanHistory(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(403).body("Достъпът е разрешен само за регистрирани потребители.");
            }

            List<NutritionPlan> plans = nutritionPlanService.getNutritionPlansByUser(user);
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            logger.error("Грешка при зареждане на историята на плановете: ", e);
            return ResponseEntity.status(500).body("Грешка при зареждане на историята");
        }
    }

    @GetMapping("/weekly/{userId}")
    // @PreAuthorize("isAuthenticated() and (#userId == authentication.principal.id or hasRole('ADMIN'))") // Example security
    public ResponseEntity<?> generateWeeklyPlan(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(403).body("Само за регистрирани потребители.");
            }

            // Note: This endpoint generates both plans, similar to /generate/{userId}
            NutritionPlan nutritionPlan = nutritionPlanService.generateNutritionPlan(user);
            TrainingPlan trainingPlan = trainingPlanService.generateAndSaveTrainingPlanForUser(user);

            return ResponseEntity.ok(new PlanBundleResponse(nutritionPlan, trainingPlan));
        } catch (Exception e) {
            logger.error("Грешка при генериране на седмичен план: ", e);
            return ResponseEntity.status(500).body("Грешка при седмичен режим: " + e.getMessage());
        }
    }

    @GetMapping("/full")
    // @PreAuthorize("isAuthenticated() and (#userId == authentication.principal.id or hasRole('ADMIN'))") // Example security
    public ResponseEntity<?> getFullPlan(@RequestParam Integer userId) {
        try {
            logger.info("В getFullPlan: userId = {}, Type = {}", userId, userId.getClass().getName());

            FullPlanDTO fullPlan = nutritionPlanService.getFullPlanByUserId(userId);
            if (fullPlan == null) {
                return ResponseEntity.status(404).body("Няма намерен план за потребителя.");
            }
            return ResponseEntity.ok(fullPlan);
        } catch (ClassCastException e) {
            logger.error("Грешка при преобразуване на тип за userId в getFullPlan: ", e);
            return ResponseEntity.status(400).body("Невалиден формат на потребителско ID.");
        } catch (Exception e) {
            logger.error("Грешка при вземане на пълен план: ", e);
            return ResponseEntity.status(500).body("Вътрешна грешка: " + e.getMessage());
        }
    }

    @GetMapping("/debug/{userId}")
    @PreAuthorize("hasRole('ADMIN')") // Debug endpoints should be admin-only
    public ResponseEntity<?> debugPlan(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) return ResponseEntity.notFound().build();

            List<NutritionPlan> plans = nutritionPlanService.getNutritionPlansByUser(user);
            NutritionPlan latestPlan = plans.stream().findFirst().orElse(null);

            return ResponseEntity.ok(latestPlan);
        } catch (Exception e) {
            logger.error("Грешка при дебъгване на план: ", e);
            return ResponseEntity.status(500).body("Грешка при дебъгване: " + e.getMessage());
        }
    }

    @GetMapping("/fix-training")
    @PreAuthorize("hasRole('ADMIN')") // Admin-only endpoint
    public ResponseEntity<?> fixTrainingPlans() {
        try {
            // КОРИГЕНА ЛОГИКА: Извикваме метода от TrainingPlanService
            trainingPlanService.fixMissingTrainingPlans();
            return ResponseEntity.ok("Липсващите тренировъчни планове са успешно генерирани.");
        } catch (Exception e) {
            logger.error("Грешка при фиксиране на липсващи тренировъчни планове: ", e);
            return ResponseEntity.status(500).body("Грешка при фиксиране: " + e.getMessage());
        }
    }
}