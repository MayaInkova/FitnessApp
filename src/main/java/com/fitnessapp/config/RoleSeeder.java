package com.fitnessapp.config;



import com.fitnessapp.model.Role;
import com.fitnessapp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepo;

    @Override
    public void run(String... args) {
        Stream.of("ROLE_USER", "ROLE_ADMIN", "ROLE_GUEST")
                .forEach(roleName ->
                        roleRepo.findByName(roleName)
                                .orElseGet(() -> roleRepo.save(Role.builder().name(roleName).build()))
                );
    }
}