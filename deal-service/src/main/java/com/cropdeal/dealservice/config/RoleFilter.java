package com.cropdeal.dealservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class RoleFilter extends OncePerRequestFilter {

    // define which path prefixes require which role
    private static final Map<String, String> PATH_ROLE_MAP = Map.of(
            "/api/farmer", "FARMER",
            "/api/dealer", "DEALER",
            "/api/admin",  "ADMIN"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/users/internal") || path.startsWith("/v3/api-docs") || path.startsWith("/actuator") || path.startsWith("/v3/api-docs/swagger-config")|| path.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        String userRole = request.getHeader("X-User-Role");

        if (userRole == null) {
            sendError(response, "Missing role header");
            return;
        }

        for (Map.Entry<String, String> entry : PATH_ROLE_MAP.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                if (!userRole.equals(entry.getValue())) {
                    sendError(response, "Access denied. Required role: " + entry.getValue());
                    return;
                }
                break;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(
            String.format("{\"status\":403,\"message\":\"%s\"}", message)
        );
    }
}