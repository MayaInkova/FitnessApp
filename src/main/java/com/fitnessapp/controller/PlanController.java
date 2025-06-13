package com.fitnessapp.controller;

import com.fitnessapp.dto.FullPlanDTO;
import com.fitnessapp.service.NutritionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class PlanController {

    private final NutritionPlanService nutritionPlanService;

    @GetMapping("/full")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<FullPlanDTO> getFullPlan(@RequestParam Integer userId) {
        FullPlanDTO dto = nutritionPlanService.getFullPlanByUserId(userId);
        return ResponseEntity.ok(dto);
    }
}