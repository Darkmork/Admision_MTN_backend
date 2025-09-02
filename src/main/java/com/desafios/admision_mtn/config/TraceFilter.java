package com.desafios.admision_mtn.config;

import com.desafios.admision_mtn.service.JwtService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class TraceFilter implements Filter {

    private final JwtService jwtService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Generate unique trace ID for this request
        String traceId = UUID.randomUUID().toString();
        String method = httpRequest.getMethod();
        String path = httpRequest.getRequestURI();
        String userAgent = httpRequest.getHeader("User-Agent");
        String clientIp = getClientIp(httpRequest);
        
        long startTime = System.currentTimeMillis();
        
        // Set MDC context for structured logging
        MDC.put("trace_id", traceId);
        MDC.put("method", method);
        MDC.put("path", path);
        MDC.put("client_ip", clientIp);
        MDC.put("user_agent", userAgent != null ? userAgent : "unknown");
        MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Try to extract user information from JWT token
        String userId = extractUserIdFromRequest(httpRequest);
        if (userId != null) {
            MDC.put("user_id", userId);
        }
        
        try {
            // Add trace ID to response headers for debugging
            httpResponse.setHeader("X-Trace-ID", traceId);
            
            // Continue with the request
            chain.doFilter(request, response);
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = httpResponse.getStatus();
            
            // Add final request metrics to MDC
            MDC.put("status_code", String.valueOf(status));
            MDC.put("duration_ms", String.valueOf(duration));
            
            // Log the request completion
            if (status >= 400) {
                log.warn("HTTP Request completed - {} {} - Status: {} - Duration: {}ms - User: {} - IP: {}", 
                    method, path, status, duration, userId != null ? userId : "anonymous", clientIp);
            } else if (path.startsWith("/actuator")) {
                // Don't log actuator endpoints at INFO level to reduce noise
                log.debug("HTTP Request completed - {} {} - Status: {} - Duration: {}ms", 
                    method, path, status, duration);
            } else {
                log.info("HTTP Request completed - {} {} - Status: {} - Duration: {}ms - User: {} - IP: {}", 
                    method, path, status, duration, userId != null ? userId : "anonymous", clientIp);
            }
            
            // Clear MDC to prevent memory leaks
            MDC.clear();
        }
    }
    
    private String extractUserIdFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtService.extractUsername(token);
                if (username != null && jwtService.isTokenValid(token, null)) {
                    return username;
                }
            }
            
            // Also check from Spring Security context if available
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                return authentication.getName();
            }
            
        } catch (Exception e) {
            // Log error but don't fail the request
            log.debug("Could not extract user ID from request: {}", e.getMessage());
        }
        return null;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}