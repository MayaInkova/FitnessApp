

package com.fitnessapp.controller;

import com.fitnessapp.dto.LoginRequest;
import com.fitnessapp.dto.RegisterSimpleRequest;
import com.fitnessapp.dto.UserResponse;
import com.fitnessapp.dto.UserProfileUpdateDto; // НОВО: Добавете този импорт за DTO за актуализация
import com.fitnessapp.model.User;
import com.fitnessapp.model.Role;
import com.fitnessapp.repository.RoleRepository;
import com.fitnessapp.security.JwtTokenProvider;
import com.fitnessapp.service.NutritionPlanService;
import com.fitnessapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final NutritionPlanService nutritionPlanService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RoleRepository roleRepository;

    @Autowired
    public UserController(UserService userService,
                          PasswordEncoder passwordEncoder,
                          NutritionPlanService nutritionPlanService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          RoleRepository roleRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.nutritionPlanService = nutritionPlanService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.roleRepository = roleRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterSimpleRequest userRequest, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        if (userService.getUserByEmail(userRequest.getEmail()) != null) {
            return new ResponseEntity<>("Потребител с този имейл вече съществува!", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setFullName(userRequest.getFullName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        // Задаване на роля ROLE_USER по подразбиране
        Optional<Role> userRoleOptional = roleRepository.findByName("ROLE_USER");
        if (userRoleOptional.isEmpty()) {
            // Това не би трябвало да се случва, ако DataInitializer работи коректно
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Грешка: Роля 'ROLE_USER' не е намерена.");
        }
        // Използвайте Set.of() за да създадете Set от един елемент
        user.setRoles(Set.of(userRoleOptional.get())); // Присвояване на ролята

        User savedUser = userService.saveUser(user);
        UserResponse response = UserResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtTokenProvider.generateToken(authentication);

            User user = userService.getUserByEmail(loginRequest.getEmail());
            UserResponse userResponse = UserResponse.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                    .build();

            return ResponseEntity.ok(Map.of("accessToken", jwt, "user", userResponse));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Невалиден имейл или парола.");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        // Обикновено този ендпойнт е защитен и достъпен само за ADMIN/MODERATOR
        // @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
        List<User> users = userService.getAllUsers();
        List<UserResponse> responses = users.stream().map(user -> UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build()).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        if (user == null) return ResponseEntity.notFound().build();

        // Проверка за достъп: Логнатият потребител може да вижда само собствения си профил
        // освен ако не е ADMIN/MODERATOR
        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isModerator = SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MODERATOR"));

        if (!user.getEmail().equals(loggedInUserEmail) && !isAdmin && !isModerator) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Нямате право да преглеждате този профил.");
        }

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();

        return ResponseEntity.ok(response);
    }

    // Обновяване на основни потребителски данни с UserProfileUpdateDto
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @Valid @RequestBody UserProfileUpdateDto userData) { // ПРОМЕНЕНО ТУК: User на UserProfileUpdateDto и добавено @Valid
        User user = userService.getUserById(id);
        if (user == null) return ResponseEntity.notFound().build();

        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (!user.getEmail().equals(loggedInUserEmail) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Нямате право да променяте този профил.");
        }

        // Извикваме новия метод в UserService за мапинг и актуализация
        User updated = userService.updateUserProfile(id, userData); // Извикване на updateUserProfile
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/diet")
    public ResponseEntity<String> updateUserDiet(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        // Проверка за достъп - подобна на updateUser
        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        User user = userService.getUserById(id);
        if (user == null) return ResponseEntity.notFound().build();
        if (!user.getEmail().equals(loggedInUserEmail) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Нямате право да променяте този профил.");
        }

        String diet = body.get("diet");
        userService.updateDietTypeForUser(id, diet);
        return ResponseEntity.ok("Diet type updated");
    }

    //  Промяна на имейл
    @PutMapping("/{id}/email")
    public ResponseEntity<?> updateEmail(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String newEmail = body.get("newEmail");
        String currentPassword = body.get("currentPassword"); // Изискваме текуща парола за потвърждение

        if (!org.springframework.util.StringUtils.hasText(newEmail) || !org.springframework.util.StringUtils.hasText(currentPassword)) {
            return ResponseEntity.badRequest().body("Моля, попълнете нов имейл и текуща парола.");
        }

        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Проверка за достъп
        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!user.getEmail().equals(loggedInUserEmail) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Нямате право да променяте този профил.");
        }

        // Проверка дали новият имейл вече не е зает от друг потребител
        User existingUserWithNewEmail = userService.getUserByEmail(newEmail);
        if (existingUserWithNewEmail != null && !existingUserWithNewEmail.getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Имейлът вече се използва от друг потребител.");
        }

        // Проверка на текущата парола
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Грешна текуща парола.");
        }

        user.setEmail(newEmail);
        userService.saveUser(user);
        return ResponseEntity.ok("Имейлът е успешно променен.");
    }

    //  Промяна на парола
    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (!org.springframework.util.StringUtils.hasText(currentPassword) || !org.springframework.util.StringUtils.hasText(newPassword)) {
            return ResponseEntity.badRequest().body("Моля, попълнете текуща и нова парола.");
        }

        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Проверка за достъп
        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!user.getEmail().equals(loggedInUserEmail) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Нямате право да променяте този профил.");
        }

        // Проверка на текущата парола
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Невалидна текуща парола.");
        }

        // Хеширане и запазване на новата парола
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveUser(user);
        return ResponseEntity.ok("Паролата е успешно променена.");
    }
}