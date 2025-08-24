package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.service.CacheManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para gestión de cache y optimización de rendimiento
 * 
 * Proporciona endpoints administrativos para monitorear, gestionar y optimizar
 * el sistema de cache del sistema de admisión escolar.
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cache Management", description = "Gestión y optimización de cache del sistema")
@PreAuthorize("hasRole('ADMIN')")
public class CacheController {

    private final CacheManagementService cacheManagementService;

    @Operation(
        summary = "Obtener estadísticas de cache", 
        description = "Obtiene estadísticas detalladas de rendimiento de todos los caches del sistema: hit rate, miss rate, tamaño, etc.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Estadísticas de cache del sistema",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "users": {
                            "hitRate": "85.5%",
                            "missRate": "14.5%",
                            "hitCount": 1250,
                            "missCount": 212,
                            "requestCount": 1462,
                            "estimatedSize": 45,
                            "averageLoadPenalty": "2.3 ms"
                        },
                        "statistics": {
                            "hitRate": "92.1%",
                            "missRate": "7.9%",
                            "hitCount": 890,
                            "missCount": 76,
                            "estimatedSize": 12
                        },
                        "_general": {
                            "totalCaches": 9,
                            "activeCaches": 9,
                            "cacheProvider": "Caffeine"
                        }
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado - requiere rol ADMIN"
        )
    })
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStatistics() {
        try {
            Map<String, Object> stats = cacheManagementService.getCacheStatistics();
            stats.put("timestamp", LocalDateTime.now());
            
            log.info("📊 Estadísticas de cache solicitadas por administrador");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de cache", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error obteniendo estadísticas de cache", 
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Estado de salud del cache", 
        description = "Verifica el estado de salud del sistema de cache y su disponibilidad.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getCacheHealth() {
        try {
            Map<String, Object> health = cacheManagementService.getHealthStatus();
            health.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Error verificando salud del cache", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "DOWN", "error", e.getMessage(), 
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Limpiar cache específico", 
        description = "Limpia un cache específico del sistema. Útil para forzar actualización de datos.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Cache limpiado exitosamente"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Nombre de cache inválido"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado - requiere rol ADMIN"
        )
    })
    @PostMapping("/clear/{cacheName}")
    public ResponseEntity<Map<String, Object>> clearSpecificCache(
        @Parameter(description = "Nombre del cache a limpiar", required = true, 
                  example = "users")
        @PathVariable String cacheName) {
        try {
            cacheManagementService.clearCache(cacheName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Cache '" + cacheName + "' limpiado exitosamente");
            response.put("cacheName", cacheName);
            response.put("timestamp", LocalDateTime.now());
            
            log.info("🗑️ Cache '{}' limpiado por administrador", cacheName);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error limpiando cache '{}'", cacheName, e);
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error", 
                           "message", "Error limpiando cache: " + e.getMessage(),
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Limpiar todos los caches", 
        description = "Limpia todos los caches del sistema. Operación administrativa que afectará el rendimiento temporalmente.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/clear-all")
    public ResponseEntity<Map<String, Object>> clearAllCaches() {
        try {
            cacheManagementService.clearAllCaches();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Todos los caches limpiados exitosamente");
            response.put("warning", "El rendimiento puede verse afectado temporalmente");
            response.put("timestamp", LocalDateTime.now());
            
            log.warn("🗑️ Todos los caches limpiados por administrador - rendimiento temporal afectado");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error limpiando todos los caches", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "error", 
                           "message", "Error limpiando caches: " + e.getMessage(),
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Limpiar caches de usuario", 
        description = "Limpia todos los caches relacionados con un usuario específico tras actualización de datos.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/clear-user/{userEmail}")
    public ResponseEntity<Map<String, Object>> clearUserCaches(
        @Parameter(description = "Email del usuario", required = true, 
                  example = "jorge.gangale@mtn.cl")
        @PathVariable String userEmail) {
        try {
            cacheManagementService.clearUserCaches(userEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Caches de usuario limpiados exitosamente");
            response.put("userEmail", userEmail);
            response.put("timestamp", LocalDateTime.now());
            
            log.info("🗑️ Caches de usuario '{}' limpiados por administrador", userEmail);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error limpiando caches de usuario '{}'", userEmail, e);
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error", 
                           "message", "Error limpiando caches de usuario: " + e.getMessage(),
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Optimizar rendimiento de cache", 
        description = "Ejecuta optimización inteligente del cache: limpia caches con baja eficiencia, precarga datos críticos.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/optimize")
    public ResponseEntity<Map<String, Object>> optimizeCachePerformance() {
        try {
            // Obtener estadísticas previas
            Map<String, Object> statsBefore = cacheManagementService.getCacheStatistics();
            
            // Ejecutar optimización
            cacheManagementService.optimizeCachePerformance();
            
            // Ejecutar precarga de datos críticos
            cacheManagementService.preloadCriticalData();
            
            // Obtener estadísticas posteriores
            Map<String, Object> statsAfter = cacheManagementService.getCacheStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Optimización de cache completada exitosamente");
            response.put("statsBefore", statsBefore);
            response.put("statsAfter", statsAfter);
            response.put("timestamp", LocalDateTime.now());
            
            log.info("🚀 Optimización de cache ejecutada por administrador");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error en optimización de cache", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "error", 
                           "message", "Error en optimización: " + e.getMessage(),
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Limpiar estadísticas de cache", 
        description = "Limpia específicamente el cache de estadísticas tras cambios importantes en los datos.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/clear-statistics")
    public ResponseEntity<Map<String, Object>> clearStatisticsCache() {
        try {
            cacheManagementService.clearStatisticsCaches();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Cache de estadísticas limpiado exitosamente");
            response.put("info", "Los dashboards mostrarán datos actualizados en la próxima consulta");
            response.put("timestamp", LocalDateTime.now());
            
            log.info("📊 Cache de estadísticas limpiado por administrador");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error limpiando cache de estadísticas", e);
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error", 
                           "message", "Error limpiando estadísticas: " + e.getMessage(),
                           "timestamp", LocalDateTime.now()));
        }
    }
}