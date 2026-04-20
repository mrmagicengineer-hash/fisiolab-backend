package com.app.fisiolab_system.security;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Simple in-memory sliding-window rate limiter.
 * Applied to authentication endpoints to mitigate brute-force and
 * credential-stuffing attacks. Limits are per client IP address.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${rate-limit.auth.capacity:10}")
    private int capacity;

    @Value("${rate-limit.auth.window-seconds:60}")
    private int windowSeconds;

    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && path.startsWith(context)) {
            path = path.substring(context.length());
        }
        return !path.startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String clientKey = resolveClientKey(request);
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000L);

        Deque<Long> timestamps = requestLog.computeIfAbsent(clientKey, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            Iterator<Long> it = timestamps.iterator();
            while (it.hasNext()) {
                if (it.next() < windowStart) {
                    it.remove();
                } else {
                    break;
                }
            }

            if (timestamps.size() >= capacity) {
                writeTooManyRequests(response);
                return;
            }

            timestamps.addLast(now);
        }

        if (requestLog.size() > 10_000) {
            pruneStaleEntries(windowStart);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.setHeader("Retry-After", String.valueOf(windowSeconds));

        Map<String, Object> body = Map.of(
                "status", 429,
                "error", "Too Many Requests",
                "message", "Has excedido el limite de peticiones permitidas. Intenta nuevamente en " + windowSeconds + " segundos."
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private void pruneStaleEntries(long windowStart) {
        requestLog.entrySet().removeIf(entry -> {
            Deque<Long> queue = entry.getValue();
            synchronized (queue) {
                return queue.isEmpty() || queue.peekLast() < windowStart;
            }
        });
    }
}
