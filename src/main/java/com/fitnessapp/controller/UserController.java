package com.fitnessapp.controller;


import com.fitnessapp.dto.LoginRequest;
import com.fitnessapp.dto.UserRequest;
import com.fitnessapp.dto.UserResponse;
import com.fitnessapp.model.NutritionPlan;
import com.fitnessapp.model.User;
import com.fitnessapp.service.NutritionPlanService;
import com.fitnessapp.service.UserService;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")


public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final NutritionPlanService nutritionPlanService;

    @Autowired
    public UserController(UserService userService,
                          PasswordEncoder passwordEncoder,
                          NutritionPlanService nutritionPlanService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.nutritionPlanService = nutritionPlanService;
    }


    @PostMapping("/register")

    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequest userRequest, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        User user = new User();
        user.setFullName(userRequest.getFullName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setAge(userRequest.getAge());
        user.setHeight(userRequest.getHeight());
        user.setWeight(userRequest.getWeight());
        user.setGender(userRequest.getGender());
        user.setActivityLevel(userRequest.getActivityLevel());
        user.setGoal(userRequest.getGoal());

        User savedUser = userService.saveUser(user);

        //  Генерираме хранителен план
        nutritionPlanService.generatePlanForUser(savedUser);

        UserResponse response = UserResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .age(savedUser.getAge())
                .height(savedUser.getHeight())
                .weight(savedUser.getWeight())
                .gender(savedUser.getGender())
                .activityLevel(savedUser.getActivityLevel())
                .goal(savedUser.getGoal())
                .build();

        return ResponseEntity.ok(response);
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        User user = userService.getUserByEmail(loginRequest.getEmail());
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Невалидни данни за вход");
        }

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .age(user.getAge())
                .height(user.getHeight())
                .weight(user.getWeight())
                .gender(user.getGender())
                .activityLevel(user.getActivityLevel())
                .goal(user.getGoal())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponse> responses = users.stream().map(user -> UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .age(user.getAge())
                .height(user.getHeight())
                .weight(user.getWeight())
                .gender(user.getGender())
                .activityLevel(user.getActivityLevel())
                .goal(user.getGoal())
                .build()).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) return ResponseEntity.notFound().build();

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .age(user.getAge())
                .height(user.getHeight())
                .weight(user.getWeight())
                .gender(user.getGender())
                .activityLevel(user.getActivityLevel())
                .goal(user.getGoal())
                .build();

        return ResponseEntity.ok(response);
    }
}