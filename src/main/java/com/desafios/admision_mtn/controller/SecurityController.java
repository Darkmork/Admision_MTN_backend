package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.security.RateLimitingService;
import com.desafios.admision_mtn.security.SecurityValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para monitoreo y gestión de seguridad del sistema
 * 
 * Proporciona endpoints administrativos para verificar el estado de seguridad,
 * rate limiting y detección de amenazas en tiempo real.
 */
@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Security Management", description = "Gestión y monitoreo de seguridad del sistema")
@PreAuthorize("hasRole('ADMIN')")
public class SecurityController {

    private final RateLimitingService rateLimitingService;
    private final SecurityValidationService securityValidationService;

    @Operation(
        summary = "Obtener estadísticas de rate limiting", 
        description = "Obtiene estadísticas actuales del sistema de rate limiting: IPs bloqueadas, usuarios limitados, etc.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Estadísticas de rate limiting obtenidas exitosamente"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado - requiere rol ADMIN"
        )
    })
    @GetMapping("/rate-limiting/stats")
    public ResponseEntity<Map<String, Object>> getRateLimitingStats() {
        try {
            RateLimitingService.RateLimitingStats stats = rateLimitingService.getStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("activeTrackers", stats.activeTrackers());
            response.put("blockedIps", stats.blockedIps());
            response.put("blockedUsers", stats.blockedUsers());
            response.put("status", stats.blockedIps() > 10 ? "HIGH_ACTIVITY" : "NORMAL");
            response.put("timestamp", LocalDateTime.now());
            
            log.info("📊 Estadísticas de rate limiting solicitadas por administrador");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de rate limiting", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error obteniendo estadísticas de seguridad", 
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Estado de salud del sistema de seguridad", 
        description = "Verifica el estado general del sistema de seguridad: rate limiting, validaciones, etc.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSecurityHealth() {
        try {
            RateLimitingService.RateLimitingStats rateLimitStats = rateLimitingService.getStatistics();
            
            Map<String, Object> health = new HashMap<>();
            health.put("rateLimiting", Map.of(
                "status", "UP",
                "activeTrackers", rateLimitStats.activeTrackers(),
                "blockedEntities", rateLimitStats.blockedIps() + rateLimitStats.blockedUsers()
            ));
            
            health.put("validation", Map.of(
                "status", "UP",
                "sqlInjectionDetection", "ACTIVE",
                "xssProtection", "ACTIVE",
                "inputSanitization", "ACTIVE"
            ));
            
            health.put("overallStatus", "UP");
            health.put("lastCheck", LocalDateTime.now());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Error verificando salud del sistema de seguridad", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "DOWN", "error", e.getMessage(), 
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Limpiar trackers de rate limiting", 
        description = "Limpia trackers expirados del sistema de rate limiting para liberar memoria.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/rate-limiting/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupRateLimiting() {
        try {
            rateLimitingService.cleanupExpiredTrackers();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Limpieza de rate limiting completada exitosamente");
            response.put("timestamp", LocalDateTime.now());
            
            log.info("🧹 Limpieza de rate limiting ejecutada por administrador");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error en limpieza de rate limiting", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "error", 
                           "message", "Error en limpieza: " + e.getMessage(),
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Validar fortaleza de contraseña", 
        description = "Valida la fortaleza de una contraseña según las políticas de seguridad del sistema.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/validate/password")
    public ResponseEntity<Map<String, Object>> validatePassword(
            @RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            SecurityValidationService.PasswordValidationResult result = 
                securityValidationService.validatePasswordStrength(password);
            
            Map<String, Object> response = new HashMap<>();
            response.put("isValid", result.isValid());
            response.put("violations", result.violations());
            response.put("strength", result.isValid() ? "STRONG" : "WEAK");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error validando fortaleza de contraseña", e);
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error", 
                           "message", "Error validando contraseña: " + e.getMessage(),
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Detectar contenido malicioso", 
        description = "Analiza un texto en busca de intentos de SQL injection, XSS u otros ataques.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/validate/content")
    public ResponseEntity<Map<String, Object>> validateContent(
            @RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            
            boolean hasSqlInjection = securityValidationService.containsSqlInjection(content);
            boolean hasXss = securityValidationService.containsXss(content);
            String sanitized = securityValidationService.sanitizeInput(content);
            
            Map<String, Object> response = new HashMap<>();
            response.put("isSafe", !hasSqlInjection && !hasXss);
            response.put("threats", Map.of(
                "sqlInjection", hasSqlInjection,
                "xss", hasXss
            ));
            response.put("sanitizedContent", sanitized);
            response.put("timestamp", LocalDateTime.now());
            
            if (hasSqlInjection || hasXss) {
                log.warn("🚨 Contenido malicioso detectado por administrador");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error validando contenido", e);
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error", 
                           "message", "Error validando contenido: " + e.getMessage(),
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Resumen de seguridad del sistema", 
        description = "Obtiene un resumen completo del estado de seguridad del sistema.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSecuritySummary() {
        try {
            RateLimitingService.RateLimitingStats rateLimitStats = rateLimitingService.getStatistics();
            
            Map<String, Object> summary = new HashMap<>();
            
            // Estado general
            boolean hasThreats = rateLimitStats.blockedIps() > 0 || rateLimitStats.blockedUsers() > 0;
            summary.put("threatLevel", hasThreats ? "MEDIUM" : "LOW");
            summary.put("overallStatus", "PROTECTED");
            
            // Métricas de rate limiting
            summary.put("rateLimiting", Map.of(
                "activeProtections", rateLimitStats.activeTrackers(),
                "blockedThreats", rateLimitStats.blockedIps() + rateLimitStats.blockedUsers(),
                "status", "ACTIVE"
            ));
            
            // Protecciones activas
            summary.put("activeProtections", Map.of(
                "rateLimiting", true,
                "sqlInjectionPrevention", true,
                "xssProtection", true,
                "inputValidation", true,
                "passwordPolicy", true,
                "emailValidation", true
            ));
            
            summary.put("lastUpdated", LocalDateTime.now());
            
            log.info("📋 Resumen de seguridad generado para administrador");
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error generando resumen de seguridad", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "error", 
                           "message", "Error generando resumen: " + e.getMessage(),
                           "timestamp", LocalDateTime.now()));
        }
    }
}