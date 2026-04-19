package com.cropdeal.notificationservice.config;
 
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
 
import java.io.IOException;
 
@Slf4j
@Component
@Order(1)
public class RequestResponseLoggingFilter implements Filter {
 
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
 
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
 
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);
 
        long startTime = System.currentTimeMillis();
 
        log.info(">>> INCOMING REQUEST");
        log.info("    Method  : {}", httpRequest.getMethod());
        log.info("    URI     : {}", httpRequest.getRequestURI());
        log.info("    Role    : {}", httpRequest.getHeader("X-User-Role"));
        log.info("    User ID : {}", httpRequest.getHeader("X-User-Id"));
 
        chain.doFilter(wrappedRequest, wrappedResponse);
 
        long duration = System.currentTimeMillis() - startTime;
 
        String requestBody = getRequestBody(wrappedRequest);
        if (!requestBody.isBlank()) {
            log.info("    Body    : {}", requestBody);
        }
 
        String responseBody = getResponseBody(wrappedResponse);
 
        log.info("<<< OUTGOING RESPONSE");
        log.info("    Status  : {}", wrappedResponse.getStatus());
        log.info("    Duration: {} ms", duration);
        if (!responseBody.isBlank()) {
            log.info("    Body    : {}", responseBody);
        }
        log.info("------------------------------------------------------");
 
        wrappedResponse.copyBodyToResponse();
    }
 
    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) return "";
        return new String(content).trim();
    }
 
    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) return "";
        String body = new String(content).trim();
        return body.length() > 500 ? body.substring(0, 500) + "... [truncated]" : body;
    }
}