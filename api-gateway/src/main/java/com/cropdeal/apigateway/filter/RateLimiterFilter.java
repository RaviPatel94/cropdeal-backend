package com.cropdeal.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimiterFilter implements GlobalFilter, Ordered {

    private final ConcurrentHashMap<String, RequestCount> requestCounts = new ConcurrentHashMap<>();

    private Instant lastCleanup = Instant.now();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().toString();
        String key = resolveKey(exchange);
        int limit = resolveLimit(path);

        cleanup();

        RequestCount count = requestCounts.computeIfAbsent(
                key, k -> new RequestCount());

        if (count.isExpired()) {
            count.reset();
        }
        count.increment();
        log.info("count : "+count.getCount());
        log.info("key : "+key);
        if (count.getCount() > limit) {
        	
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders()
                    .add("X-RateLimit-Limit", String.valueOf(limit));
            exchange.getResponse().getHeaders()
                    .add("Retry-After", "60");
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private String resolveKey(ServerWebExchange exchange) {
        // use userId if available (authenticated requests)
        String userId = exchange.getRequest()
                .getHeaders().getFirst("X-User-Id");

        if (userId != null && !userId.isBlank()) {
            return "user:" + userId;
        }

        // fallback to IP for login/register
        String ip = exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress();

        return "ip:" + ip;
    }

    private int resolveLimit(String path) {
        if (path.contains("/api/auth/login") || path.contains("/api/auth/register")) return 10;
        return 30;
    }

    private void cleanup() {
        Instant now = Instant.now();
        if (now.minusSeconds(60).isAfter(lastCleanup)) {
            requestCounts.entrySet()
                    .removeIf(entry -> entry.getValue().isExpired());
            lastCleanup = now;
        }
    }

    @Override
    public int getOrder() {
        return -2;
    }

    static class RequestCount {
        private final AtomicInteger count = new AtomicInteger(0);
        private Instant windowStart = Instant.now();

        public int increment() {
            return count.incrementAndGet();
        }
        
        public int getCount() {
        	return count.get();
        }

        public boolean isExpired() {
            return Instant.now().minusSeconds(60).isAfter(windowStart);
        }

        public void reset() {
            count.set(0);
            windowStart = Instant.now();
        }
    }
}