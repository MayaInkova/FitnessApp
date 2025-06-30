
package com.fitnessapp.service;

import com.fitnessapp.dto.UserProfileDTO;
import com.fitnessapp.mapper.UserProfileMapper;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.ActivityLevelRepository;
import com.fitnessapp.repository.DietTypeRepository;
import com.fitnessapp.repository.GoalRepository;
import com.fitnessapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ActivityLevelRepository activityRepo;
    private final DietTypeRepository dietRepo;
    private final GoalRepository goalRepo;
    private final UserProfileMapper mapper;


    @Transactional(readOnly = true)
    public UserProfileDTO getCurrentProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Потребител не е намерен с имейл: " + email));
        return mapper.toDto(user);
    }

    @Transactional
    public UserProfileDTO updateCurrentProfile(String email, UserProfileDTO incoming) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Потребител не е намерен с имейл: " + email));


        mapper.updateEntity(user, incoming);



        userRepository.save(user);

        return mapper.toDto(user);
    }
}