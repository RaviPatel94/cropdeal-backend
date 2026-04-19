package com.cropdeal.userservice.service;

import com.cropdeal.userservice.dto.*;
import com.cropdeal.userservice.model.Role;
import java.util.List;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserProfileDto getProfile(Long userId);
    UserProfileDto updateProfile(Long userId, UserProfileDto dto);
    List<UserProfileDto> getUsersByRole(Role role);
    UserProfileDto toggleUserStatus(Long userId);
    UserProfileDto adminUpdateUser(Long userId, UserProfileDto dto);
    UserProfileDto getUserByIdInternal(Long userId);
}