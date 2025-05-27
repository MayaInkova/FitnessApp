package com.fitnessapp.controller;

import com.fitnessapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStatistics() {
        return ResponseEntity.ok(" Администраторска статистика: 120 потребители, 300 режима, 45 рецепти.");
    }

    @PostMapping("/assign-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignRoleToUser(@RequestParam Integer userId, @RequestParam String role) {
        userService.assignRole(userId, role);
        return ResponseEntity.ok(" Ролята '" + role + "' е присвоена на потребител с ID " + userId);
    }
}