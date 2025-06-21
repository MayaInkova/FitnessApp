package com.fitnessapp.mapper;

import com.fitnessapp.dto.UserProfileDTO;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.ActivityLevelRepository;
import com.fitnessapp.repository.DietTypeRepository;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    private String avatarUrl;

    public UserProfileDTO toDto(User u){
        if (u == null) return null;
        return UserProfileDTO.builder()
                .weight(u.getWeight())
                .height(u.getHeight())
                .age(u.getAge())
                .gender(u.getGender())
                .activityLevelId(u.getActivityLevel()!=null? u.getActivityLevel().getId():null)
                .dietTypeId(u.getDietType()!=null? u.getDietType().getId():null)
                .meatPreference(u.getMeatPreference())
                .consumesDairy(u.getConsumesDairy())
                .allergies(u.getAllergies())
                .otherDietaryPreferences(u.getOtherDietaryPreferences())
                .mealFrequencyPreference(u.getMealFrequencyPreference())
                .build();
    }

    /** ъпдейтваме само полетата, които DTO-то носи (null → пропускаме) */
    public void updateEntity(User u, UserProfileDTO dto,
                             ActivityLevelRepository activityRepo,
                             DietTypeRepository dietRepo){
        if(dto.getWeight()!=null) u.setWeight(dto.getWeight());
        if(dto.getHeight()!=null) u.setHeight(dto.getHeight());
        if(dto.getAge()!=null)    u.setAge(dto.getAge());
        if(dto.getGender()!=null) u.setGender(dto.getGender());

        if(dto.getActivityLevelId()!=null)
            u.setActivityLevel(activityRepo.findById(dto.getActivityLevelId()).orElse(null));
        if(dto.getDietTypeId()!=null)
            u.setDietType(dietRepo.findById(dto.getDietTypeId()).orElse(null));

        if(dto.getMeatPreference()!=null) u.setMeatPreference(dto.getMeatPreference());
        if(dto.getConsumesDairy()!=null) u.setConsumesDairy(dto.getConsumesDairy());

        if(dto.getAllergies()!=null)               u.setAllergies(dto.getAllergies());
        if(dto.getOtherDietaryPreferences()!=null) u.setOtherDietaryPreferences(dto.getOtherDietaryPreferences());
        if(dto.getMealFrequencyPreference()!=null) u.setMealFrequencyPreference(dto.getMealFrequencyPreference());
    }
}