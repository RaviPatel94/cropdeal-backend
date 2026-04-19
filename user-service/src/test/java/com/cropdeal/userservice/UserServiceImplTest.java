package com.cropdeal.userservice;

import com.cropdeal.userservice.dto.*;
import com.cropdeal.userservice.jwt.JwtServiceI;
import com.cropdeal.userservice.model.User;
import com.cropdeal.userservice.model.Role;
import com.cropdeal.userservice.repository.UserRepository;
import com.cropdeal.userservice.service.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtServiceI jwtService;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .name("Ramesh Kumar")
                .email("ramesh@farmer.com")
                .password("encodedPassword")
                .phone("9876543210")
                .role(Role.FARMER)
                .active(true)
                .provider("LOCAL")
                .build();
    }

    @Test
    void register_ShouldSaveAndReturnToken() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Ramesh Kumar");
        request.setEmail("ramesh@farmer.com");
        request.setPassword("password123");
        request.setPhone("9876543210");
        request.setRole(Role.FARMER);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(mockUser);
        AuthResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("FARMER", response.getRole());
        verify(userRepository).save(any());
    }

    @Test
    void register_DuplicateEmail_ShouldThrowException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("ramesh@farmer.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ValidCredentials_ShouldReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ramesh@farmer.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyLong(), anyString())).thenReturn("jwt-token");

        AuthResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void login_WrongPassword_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ramesh@farmer.com");
        request.setPassword("wrongpassword");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.login(request));
    }

    @Test
    void login_UserNotFound_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@email.com");
        request.setPassword("password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.login(request));
    }

    @Test
    void getProfile_ShouldReturnProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        UserProfileDto profile = userService.getProfile(1L);

        assertNotNull(profile);
        assertEquals("Ramesh Kumar", profile.getName());
        assertEquals(Role.FARMER, profile.getRole());
    }

    @Test
    void getProfile_InvalidId_ShouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getProfile(99L));
    }

    @Test
    void updateProfile_ShouldUpdateAndReturn() {
        UserProfileDto dto = new UserProfileDto();
        dto.setName("Updated Name");
        dto.setPhone("9999999999");
        dto.setAddress("New Address");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any())).thenReturn(mockUser);

        UserProfileDto result = userService.updateProfile(1L, dto);

        assertNotNull(result);
        verify(userRepository).save(any());
    }

    @Test
    void toggleUserStatus_ShouldFlipStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any())).thenReturn(mockUser);

        userService.toggleUserStatus(1L);

        verify(userRepository).save(argThat(u -> !u.isActive()));
    }

    @Test
    void getUsersByRole_ShouldReturnList() {
        when(userRepository.findAllByRole(Role.FARMER)).thenReturn(List.of(mockUser));

        List<UserProfileDto> result = userService.getUsersByRole(Role.FARMER);

        assertEquals(1, result.size());
        assertEquals("Ramesh Kumar", result.get(0).getName());
    }
}