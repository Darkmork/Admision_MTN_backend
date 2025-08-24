package com.desafios.admision_mtn.security;

import com.desafios.admision_mtn.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filtro de rate limiting que intercepta peticiones HTTP
 * 
 * Aplica limitaciones de velocidad en endpoints críticos del sistema
 * basándose en IP del cliente y usuario autenticado.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter implements Filter {
    
    private final RateLimitingService rateLimitingService;
    private final JwtUtil jwtUtil;
    
    // Endpoints que requieren rate limiting especial
    private static final List<String> LOGIN_ENDPOINTS = Arrays.asList(
        "/api/auth/login", "/api/auth/professor-login"
    );
    
    private static final List<String> UPLOAD_ENDPOINTS = Arrays.asList(
        "/api/documents/upload"
    );
    
    private static final List<String> PASSWORD_ENDPOINTS = Arrays.asList(
        "/api/auth/reset-password", "/api/auth/forgot-password"
    );
    
    // Endpoints exentos de rate limiting
    private static final List<String> EXEMPT_ENDPOINTS = Arrays.asList(
        "/actuator/", "/swagger-", "/api-docs", "/health", "/error"
    );
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestPath = httpRequest.getRequestURI();
        String clientIp = getClientIpAddress(httpRequest);
        
        // Verificar si el endpoint está exento
        if (isExemptEndpoint(requestPath)) {
            chain.doFilter(request, response);
            return;
        }
        
        try {
            // Rate limiting específico por tipo de endpoint
            if (isLoginEndpoint(requestPath)) {
                if (!rateLimitingService.isLoginAllowed(clientIp)) {
                    handleRateLimitExceeded(httpResponse, "login", clientIp);
                    return;
                }
            }
            
            else if (isUploadEndpoint(requestPath)) {
                String userEmail = extractUserEmail(httpRequest);
                if (userEmail != null && !rateLimitingService.isFileUploadAllowed(userEmail)) {
                    handleRateLimitExceeded(httpResponse, "file_upload", userEmail);
                    return;
                }
            }
            
            else if (isPasswordEndpoint(requestPath)) {
                // Para password reset, usar el email del request body si está disponible
                if (!rateLimitingService.isPasswordResetAllowed(clientIp)) {
                    handleRateLimitExceeded(httpResponse, "password_reset", clientIp);
                    return;
                }
            }
            
            // Rate limiting general para APIs autenticadas
            else {
                String userEmail = extractUserEmail(httpRequest);
                if (userEmail != null && !rateLimitingService.isApiRequestAllowed(userEmail)) {
                    handleRateLimitExceeded(httpResponse, "api", userEmail);
                    return;
                }
                
                // Registrar la petición para tracking
                if (userEmail != null) {
                    rateLimitingService.recordApiRequest(userEmail);
                }
            }
            
            // Si todo está OK, continuar con la cadena de filtros
            chain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("Error en rate limiting filter para {}", requestPath, e);
            // En caso de error, permitir la petición para no bloquear el sistema
            chain.doFilter(request, response);
        }
    }
    
    /**
     * Extrae el email del usuario del JWT token
     */
    private String extractUserEmail(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return jwtUtil.extractUsername(token);
            }
        } catch (Exception e) {
            log.debug("No se pudo extraer email del token: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Obtiene la IP real del cliente considerando proxies
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        
        String xRealIpHeader = request.getHeader("X-Real-IP");
        if (xRealIpHeader != null && !xRealIpHeader.isEmpty()) {
            return xRealIpHeader;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Maneja cuando se excede el límite de peticiones
     */
    private void handleRateLimitExceeded(HttpServletResponse response, 
                                       String limitType, String identifier) throws IOException {
        
        log.warn("🚨 Rate limit excedido - Tipo: {}, Identificador: {}", limitType, identifier);
        
        response.setStatus(429); // Too Many Requests
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            """
            {
                "error": "Rate limit exceeded",
                "message": "Demasiadas peticiones. Intente nuevamente más tarde.",
                "type": "%s",
                "retryAfter": %d
            }
            """,
            limitType,
            getRetryAfterSeconds(limitType)
        );
        
        response.getWriter().write(jsonResponse);
    }
    
    /**
     * Obtiene el tiempo de espera en segundos según el tipo de límite
     */
    private int getRetryAfterSeconds(String limitType) {
        return switch (limitType) {
            case "login" -> 900; // 15 minutos
            case "api" -> 60;    // 1 minuto
            case "file_upload" -> 3600; // 1 hora
            case "password_reset" -> 3600; // 1 hora
            default -> 300; // 5 minutos por defecto
        };
    }
    
    /**
     * Verifica si es un endpoint de login
     */
    private boolean isLoginEndpoint(String path) {
        return LOGIN_ENDPOINTS.stream().anyMatch(path::contains);
    }
    
    /**
     * Verifica si es un endpoint de upload
     */
    private boolean isUploadEndpoint(String path) {
        return UPLOAD_ENDPOINTS.stream().anyMatch(path::contains);
    }
    
    /**
     * Verifica si es un endpoint de password
     */
    private boolean isPasswordEndpoint(String path) {
        return PASSWORD_ENDPOINTS.stream().anyMatch(path::contains);
    }
    
    /**
     * Verifica si el endpoint está exento de rate limiting
     */
    private boolean isExemptEndpoint(String path) {
        return EXEMPT_ENDPOINTS.stream().anyMatch(path::contains);
    }
}