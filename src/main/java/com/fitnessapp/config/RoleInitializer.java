package com.fitnessapp.config;

import com.fitnessapp.model.Role;
import com.fitnessapp.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoleInitializer {

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName("ROLE_USER").isEmpty()) {
                roleRepository.save(Role.builder().name("ROLE_USER").build());
                roleRepository.save(Role.builder().name("ROLE_MODERATOR").build());
                roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
                System.out.println(" Ролите са инициализирани успешно.");
            }
        };
    }
}