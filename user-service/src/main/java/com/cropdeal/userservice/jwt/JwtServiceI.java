package com.cropdeal.userservice.jwt;

public interface JwtServiceI {
	String generateToken(String email, Long userId, String role);
}
