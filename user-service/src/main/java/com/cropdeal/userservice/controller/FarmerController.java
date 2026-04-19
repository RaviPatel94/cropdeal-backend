package com.cropdeal.userservice.controller;

import com.cropdeal.userservice.dto.*;
import com.cropdeal.userservice.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/farmer")
@RequiredArgsConstructor
public class FarmerController {

    private final UserServiceImpl userService;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDto> updateProfile(@PathVariable Long userId,
                                                         @RequestBody UserProfileDto dto) {
        return ResponseEntity.ok(userService.updateProfile(userId, dto));
    }


}