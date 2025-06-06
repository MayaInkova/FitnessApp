package com.fitnessapp.service;

import com.fitnessapp.model.DietType;
import com.fitnessapp.model.Role;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.DietTypeRepository;
import com.fitnessapp.repository.RoleRepository;
import com.fitnessapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DietTypeRepository dietTypeRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       DietTypeRepository dietTypeRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.dietTypeRepository = dietTypeRepository;
    }

    public User saveUser(User user) {
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Ролята не е намерена"));


        List<Role> roles = new ArrayList<>();
        roles.add(userRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    public void assignRole(Integer userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Потребителят не е намерен"));

        Role role = roleRepository.findByName("ROLE_" + roleName.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Ролята не е намерена"));

        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>());
        }

        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            userRepository.save(user);
        }
    }

    public void updateDietTypeForUser(Integer userId, String dietName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DietType dietType = dietTypeRepository.findByNameIgnoreCase(dietName)
                .orElseThrow(() -> new RuntimeException("Unknown diet type"));

        user.setDietType(dietType);
        userRepository.save(user);
    }
}