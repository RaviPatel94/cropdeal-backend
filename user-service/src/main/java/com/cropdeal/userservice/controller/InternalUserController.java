package com.cropdeal.userservice.controller;

import com.cropdeal.userservice.dto.UserProfileDto;
import com.cropdeal.userservice.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserServiceImpl userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDto> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserByIdInternal(userId));
    }
}