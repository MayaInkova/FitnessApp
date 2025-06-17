package com.fitnessapp.controller;

import com.fitnessapp.dto.FullPlanDTO;
import com.fitnessapp.dto.PlanBundleResponse;
import com.fitnessapp.dto.NutritionPlanDTO;
import com.fitnessapp.dto.TrainingPlanDTO;

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

@RestController
@RequestMapping("/api/nutrition-plans")
@CrossOrigin(origins = "*") // Разрешаване на CORS за всички произходи
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

            NutritionPlanDTO savedPlanDTO = nutritionPlanService.saveNutritionPlan(plan);
            return ResponseEntity.ok(savedPlanDTO);
        } catch (Exception e) {
            logger.error("Грешка при създаване на хранителен план: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Грешка при създаване на план: " + e.getMessage());
        }
    }


    @PostMapping("/generate/{userId}")
    public ResponseEntity<?> generatePlan(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId); // Assuming this returns User Entity, which is fine for internal use
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Потребител с ID " + userId + " не е намерен.");
            }


            NutritionPlanDTO nutritionPlanDTO = nutritionPlanService.generateAndSaveNutritionPlanForUserDTO(user);

            TrainingPlanDTO trainingPlanDTO = trainingPlanService.generateAndSaveTrainingPlanForUserDTO(user);


            return ResponseEntity.ok(new PlanBundleResponse(nutritionPlanDTO, trainingPlanDTO));
        } catch (IllegalArgumentException e) {
            logger.warn("Липсващи потребителски данни за генериране на план: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Грешка при генериране на план за потребител {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Възникна вътрешна грешка при генериране на план: " + e.getMessage());
        }
    }


    @GetMapping("/{userId}")
    public ResponseEntity<?> getNutritionPlanByUserId(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Потребител с ID " + userId + " не е намерен.");
            }


            List<NutritionPlanDTO> plansDTO = nutritionPlanService.getNutritionPlansByUserDTO(user);
            NutritionPlanDTO latestPlanDTO = plansDTO.stream().findFirst().orElse(null);
            return latestPlanDTO != null ? ResponseEntity.ok(latestPlanDTO) : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Няма намерен хранителен план за потребител с ID " + userId);
        } catch (Exception e) {
            logger.error("Грешка при вземане на план по потребител {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Възникна грешка при вземане на план: " + e.getMessage());
        }
    }


    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllNutritionPlans() {
        try {

            List<NutritionPlanDTO> plansDTO = nutritionPlanService.getAllNutritionPlansDTO();
            return ResponseEntity.ok(plansDTO);
        } catch (Exception e) {
            logger.error("Грешка при зареждане на всички планове: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Грешка при зареждане: " + e.getMessage());
        }
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getPlanHistory(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Потребител с ID " + userId + " не е намерен.");
            }


            List<NutritionPlanDTO> plansDTO = nutritionPlanService.getNutritionPlansByUserDTO(user);
            return ResponseEntity.ok(plansDTO);
        } catch (Exception e) {
            logger.error("Грешка при зареждане на историята на плановете за потребител {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Грешка при зареждане на историята");
        }
    }


    @GetMapping("/weekly/{userId}")
    public ResponseEntity<?> generateWeeklyPlan(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Потребител с ID " + userId + " не е намерен.");
            }


            NutritionPlanDTO nutritionPlanDTO = nutritionPlanService.generateAndSaveNutritionPlanForUserDTO(user);

            TrainingPlanDTO trainingPlanDTO = trainingPlanService.generateAndSaveTrainingPlanForUserDTO(user);


            return ResponseEntity.ok(new PlanBundleResponse(nutritionPlanDTO, trainingPlanDTO));
        } catch (IllegalArgumentException e) {
            logger.warn("Липсващи потребителски данни за генериране на седмичен план: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Грешка при генериране на седмичен план за потребител {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Грешка при генериране на седмичен режим: " + e.getMessage());
        }
    }


    @GetMapping("/full")
    public ResponseEntity<?> getFullPlan(@RequestParam Integer userId) {
        try {
            logger.info("В getFullPlan: userId = {}, Type = {}", userId, userId.getClass().getName());

            FullPlanDTO fullPlan = nutritionPlanService.getFullPlanByUserId(userId);
            if (fullPlan == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Няма намерен план за потребителя.");
            }
            return ResponseEntity.ok(fullPlan);
        } catch (ClassCastException e) {
            logger.error("Грешка при преобразуване на тип за userId в getFullPlan: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Невалиден формат на потребителско ID.");
        } catch (Exception e) {
            logger.error("Грешка при вземане на пълен план за потребител {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Вътрешна грешка при вземане на пълен план: " + e.getMessage());
        }
    }


    @GetMapping("/debug/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> debugPlan(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Потребител с ID " + userId + " не е намерен.");
            }


            List<NutritionPlanDTO> plansDTO = nutritionPlanService.getNutritionPlansByUserDTO(user);
            NutritionPlanDTO latestPlanDTO = plansDTO.stream().findFirst().orElse(null);

            return ResponseEntity.ok(latestPlanDTO);
        } catch (Exception e) {
            logger.error("Грешка при дебъгване на план за потребител {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Грешка при дебъгване: " + e.getMessage());
        }
    }


    @GetMapping("/fix-training")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> fixTrainingPlans() {
        try {
            trainingPlanService.fixMissingTrainingPlans();
            return ResponseEntity.ok("Липсващите тренировъчни планове са успешно генерирани.");
        } catch (Exception e) {
            logger.error("Грешка при фиксиране на липсващи тренировъчни планове: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Грешка при фиксиране: " + e.getMessage());
        }
    }
}