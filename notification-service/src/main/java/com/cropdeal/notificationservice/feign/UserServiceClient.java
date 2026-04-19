package com.cropdeal.notificationservice.feign;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/internal/{userId}")
    UserDto getUserById(@PathVariable Long userId);

    @Data
    class UserDto {
        private Long id;
        private String name;
        private String email;
    }
}