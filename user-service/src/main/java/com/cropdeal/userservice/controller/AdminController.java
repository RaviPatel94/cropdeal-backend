package com.cropdeal.userservice.controller;

import com.cropdeal.userservice.dto.UserProfileDto;
import com.cropdeal.userservice.model.Role;
import com.cropdeal.userservice.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserServiceImpl userService;

    @GetMapping("/farmers")
    public ResponseEntity<List<UserProfileDto>> getAllFarmers() {
        return ResponseEntity.ok(userService.getUsersByRole(Role.FARMER));
    }

    @GetMapping("/dealers")
    public ResponseEntity<List<UserProfileDto>> getAllDealers() {
        return ResponseEntity.ok(userService.getUsersByRole(Role.DEALER));
    }

    @PutMapping("/users/{userId}/toggle-status")
    public ResponseEntity<UserProfileDto> toggleUserStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.toggleUserStatus(userId));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserProfileDto> updateUser(@PathVariable Long userId,
                                                      @RequestBody UserProfileDto dto) {
        return ResponseEntity.ok(userService.adminUpdateUser(userId, dto));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserProfileDto> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }
}