package com.fitnessapp.service;

import com.fitnessapp.model.Role;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.RoleRepository;
import com.fitnessapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public User createGuest() {
        Role guestRole = roleRepository.findByName("ROLE_GUEST")
                .orElseThrow(() -> new RuntimeException("Ролята GUEST не е намерена"));

        User guest = User.builder()
                .fullName("Гост")
                .email("guest_" + UUID.randomUUID() + "@fitnessapp.local")
                .password("") // няма нужда от парола
                .roles(Set.of(guestRole))
                .build();

        return userRepository.save(guest);
    }
}