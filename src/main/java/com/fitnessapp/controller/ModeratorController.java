package com.fitnessapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/moderator")
@CrossOrigin(origins = "*")
public class ModeratorController {

    @GetMapping("/review")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<?> reviewRecipes() {
        return ResponseEntity.ok(" Списък с рецепти за одобрение.");
    }
}