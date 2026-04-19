package com.cropdeal.userservice.service;

import com.cropdeal.userservice.dto.*;
import com.cropdeal.userservice.jwt.JwtServiceI;
import com.cropdeal.userservice.model.User;
import com.cropdeal.userservice.model.Role;
import com.cropdeal.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServiceI jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(request.getRole())
                .active(true)
                .provider("LOCAL")
                .build();
        User saved = userRepository.save(user);
        String token = "";
        return new AuthResponse(token, saved.getRole().name(), saved.getId(), saved.getName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), user.getId(), user.getName());
    }

    public UserProfileDto getProfile(Long userId) {
        return mapToDto(getUserById(userId));
    }

    public UserProfileDto updateProfile(Long userId, UserProfileDto dto) {
        User user = getUserById(userId);
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setProfilePicture(dto.getProfilePicture());
        return mapToDto(userRepository.save(user));
    }
    
    public UserProfileDto adminUpdateUser(Long userId, UserProfileDto dto) {
        return updateProfile(userId, dto);
    }

    public List<UserProfileDto> getUsersByRole(Role role) {
        return userRepository.findAllByRole(role).stream()
                .map(this::mapToDto).collect(Collectors.toList());
    }

    public UserProfileDto toggleUserStatus(Long userId) {
        User user = getUserById(userId);
        user.setActive(!user.isActive());
        return mapToDto(userRepository.save(user));
    }

    public UserProfileDto getUserByIdInternal(Long userId) {
        return mapToDto(getUserById(userId));
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    private UserProfileDto mapToDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        return dto;
    }
}