package com.cropdeal.inventoryservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-User-Role", "INTERNAL");
            requestTemplate.header("X-User-Id", "0");
        };
    }
}