package com.cropdeal.userservice.dto;

import com.cropdeal.userservice.model.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;

    @Email @NotBlank
    private String email;

    @NotBlank @Size(min = 6)
    private String password;

    private String phone;
    private String address;

    @NotNull
    private Role role;
}