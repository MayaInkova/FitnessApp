package com.fitnessapp.controller;

import com.fitnessapp.dto.PlanHistoryResponseDTO;
import com.fitnessapp.dto.NutritionPlanHistoryDTO;
import com.fitnessapp.dto.TrainingPlanHistoryDTO;
import com.fitnessapp.service.NutritionPlanService;
import com.fitnessapp.service.TrainingPlanService;
import com.fitnessapp.service.UserService;
import com.fitnessapp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plans/history")
public class PlanHistoryController {

    @Autowired
    private NutritionPlanService nutritionPlanService;
    @Autowired
    private TrainingPlanService trainingPlanService;
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<PlanHistoryResponseDTO> getUserPlanHistory(@AuthenticationPrincipal UserDetails userDetails) {
        // Извличаме потребителското ID от UserDetails, което идва от Spring Security (JWT)
        // userDetails.getUsername() обикновено е имейлът на потребителя
        User user = userService.getUserByEmail(userDetails.getUsername()) // Използваме съществуващия метод
                .orElseThrow(() -> new RuntimeException("User not found for authenticated principal: " + userDetails.getUsername()));

        // Взимаме историята за хранителни планове
        List<NutritionPlanHistoryDTO> nutritionPlans = nutritionPlanService.getNutritionPlanHistory(user.getId());
        // Взимаме историята за тренировъчни планове
        List<TrainingPlanHistoryDTO> trainingPlans = trainingPlanService.getTrainingPlanHistory(user.getId());

        // Създаваме обект, който да съдържа и двата списъка
        PlanHistoryResponseDTO response = PlanHistoryResponseDTO.builder()
                .nutritionPlans(nutritionPlans)
                .trainingPlans(trainingPlans)
                .build();

        // Връщаме отговор с HTTP статус 200 OK
        return ResponseEntity.ok(response);
    }
}