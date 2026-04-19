package com.cropdeal.userservice.dto;

import com.cropdeal.userservice.model.Role;
import lombok.Data;

@Data
public class UserProfileDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String profilePicture;
    private Role role;
    private boolean active;
}