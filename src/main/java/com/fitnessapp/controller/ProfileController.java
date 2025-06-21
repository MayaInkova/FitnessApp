package com.fitnessapp.controller;

import com.fitnessapp.dto.UserProfileDTO;
import com.fitnessapp.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/profile")
    public UserProfileDTO getProfile(@AuthenticationPrincipal UserDetails principal){
        return profileService.getCurrentProfile(principal.getUsername());
    }

    @PutMapping("/profile")
    public UserProfileDTO saveProfile(@RequestBody UserProfileDTO dto,
                                      @AuthenticationPrincipal UserDetails principal){
        return profileService.updateCurrentProfile(principal.getUsername(), dto);
    }
}