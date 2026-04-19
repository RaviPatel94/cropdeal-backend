package com.cropdeal.userservice.repository;

import com.cropdeal.userservice.model.User;
import com.cropdeal.userservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAllByRole(Role role);
    List<User> findAllByRoleAndActive(Role role, boolean active);
}