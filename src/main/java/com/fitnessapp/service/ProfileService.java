package com.fitnessapp.service;

import com.fitnessapp.dto.UserProfileDTO;
import com.fitnessapp.mapper.UserProfileMapper;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.ActivityLevelRepository;
import com.fitnessapp.repository.DietTypeRepository;
import com.fitnessapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ActivityLevelRepository activityRepo;
    private final DietTypeRepository dietRepo;
    private final UserProfileMapper mapper;


    public UserProfileDTO getCurrentProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return mapper.toDto(user);
    }

    public UserProfileDTO updateCurrentProfile(String email, UserProfileDTO incoming) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        mapper.updateEntity(user, incoming, activityRepo, dietRepo);
        userRepository.save(user);

        return mapper.toDto(user);          // връщаме обновения профил
    }
}