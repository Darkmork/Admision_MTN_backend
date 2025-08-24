package com.desafios.admision_mtn.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Servicio para gestión avanzada de cache en el sistema de admisión
 * 
 * Proporciona operaciones de limpieza, estadísticas y gestión inteligente
 * del cache para optimizar el rendimiento del sistema.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheManagementService {

    private final CacheManager cacheManager;

    /**
     * Limpia todos los caches del sistema
     */
    @CacheEvict(value = {"users", "evaluations", "applications", "interviews", 
                        "statistics", "notifications", "documents", "workflow-states", 
                        "validation-rules"}, allEntries = true)
    public void clearAllCaches() {
        log.info("🗑️ Limpiando todos los caches del sistema");
    }

    /**
     * Limpia cache específico
     */
    public void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("🗑️ Cache '{}' limpiado exitosamente", cacheName);
        } else {
            log.warn("⚠️ Cache '{}' no encontrado", cacheName);
        }
    }

    /**
     * Limpia caches relacionados con un usuario específico
     */
    public void clearUserCaches(String userEmail) {
        Cache userCache = cacheManager.getCache("users");
        if (userCache != null) {
            userCache.evict(userEmail);
        }
        
        // Limpiar caches relacionados
        clearCache("applications");
        clearCache("evaluations");
        clearCache("interviews");
        
        log.info("🗑️ Caches de usuario '{}' limpiados", userEmail);
    }

    /**
     * Limpia caches relacionados con estadísticas tras cambios importantes
     */
    @CacheEvict(value = {"statistics"}, allEntries = true)
    public void clearStatisticsCaches() {
        log.info("📊 Caches de estadísticas limpiados");
    }

    /**
     * Pre-carga datos críticos en cache
     */
    public void preloadCriticalData() {
        log.info("⏳ Iniciando precarga de datos críticos en cache...");
        
        try {
            // Pre-cargar validation rules (datos estáticos)
            preloadValidationRules();
            
            // Pre-cargar workflow states (datos semi-estáticos)
            preloadWorkflowStates();
            
            log.info("✅ Precarga de datos críticos completada");
            
        } catch (Exception e) {
            log.error("❌ Error en precarga de datos críticos", e);
        }
    }

    /**
     * Obtiene estadísticas de uso del cache
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            String[] cacheNames = {"users", "evaluations", "applications", "interviews", 
                                 "statistics", "notifications", "documents", 
                                 "workflow-states", "validation-rules"};
            
            for (String cacheName : cacheNames) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    Map<String, Object> cacheStats = new HashMap<>();
                    
                    // Obtener estadísticas si es un cache de Caffeine
                    if (cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                        com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache = 
                            (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();
                        
                        var cacheStatsObj = caffeineCache.stats();
                        cacheStats.put("hitRate", String.format("%.2f%%", cacheStatsObj.hitRate() * 100));
                        cacheStats.put("missRate", String.format("%.2f%%", cacheStatsObj.missRate() * 100));
                        cacheStats.put("hitCount", cacheStatsObj.hitCount());
                        cacheStats.put("missCount", cacheStatsObj.missCount());
                        cacheStats.put("requestCount", cacheStatsObj.requestCount());
                        cacheStats.put("evictionCount", cacheStatsObj.evictionCount());
                        cacheStats.put("estimatedSize", caffeineCache.estimatedSize());
                        cacheStats.put("averageLoadPenalty", String.format("%.2f ms", 
                            cacheStatsObj.averageLoadPenalty() / 1_000_000.0));
                    } else {
                        cacheStats.put("status", "Estadísticas no disponibles para este tipo de cache");
                    }
                    
                    stats.put(cacheName, cacheStats);
                }
            }
            
            // Estadísticas generales
            Map<String, Object> generalStats = new HashMap<>();
            generalStats.put("totalCaches", cacheNames.length);
            generalStats.put("activeCaches", stats.size());
            generalStats.put("cacheProvider", "Caffeine");
            stats.put("_general", generalStats);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de cache", e);
            stats.put("error", "Error obteniendo estadísticas: " + e.getMessage());
        }
        
        return stats;
    }

    /**
     * Ejecuta operación con cache inteligente
     */
    public <T> T executeWithCache(String cacheName, String key, Callable<T> operation) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                log.debug("🎯 Cache hit para {}.{}", cacheName, key);
                return (T) wrapper.get();
            }
        }
        
        try {
            T result = operation.call();
            if (cache != null) {
                cache.put(key, result);
                log.debug("💾 Valor almacenado en cache {}.{}", cacheName, key);
            }
            return result;
        } catch (Exception e) {
            log.error("Error ejecutando operación con cache", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Optimiza el rendimiento del cache
     */
    public void optimizeCachePerformance() {
        log.info("🚀 Iniciando optimización de cache...");
        
        try {
            // Limpiar caches con baja tasa de aciertos
            Map<String, Object> stats = getCacheStatistics();
            
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                String cacheName = entry.getKey();
                if (!cacheName.startsWith("_") && entry.getValue() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> cacheStats = (Map<String, Object>) entry.getValue();
                    
                    if (cacheStats.containsKey("hitRate")) {
                        String hitRateStr = (String) cacheStats.get("hitRate");
                        double hitRate = Double.parseDouble(hitRateStr.replace("%", ""));
                        
                        // Si la tasa de aciertos es muy baja, limpiar el cache
                        if (hitRate < 30.0) {
                            clearCache(cacheName);
                            log.info("🧹 Cache '{}' limpiado por baja tasa de aciertos: {}%", 
                                   cacheName, hitRate);
                        }
                    }
                }
            }
            
            log.info("✅ Optimización de cache completada");
            
        } catch (Exception e) {
            log.error("❌ Error en optimización de cache", e);
        }
    }

    /**
     * Pre-carga reglas de validación
     */
    private void preloadValidationRules() {
        // Implementar precarga de reglas de validación
        log.debug("📋 Precargando reglas de validación...");
    }

    /**
     * Pre-carga estados de workflow
     */
    private void preloadWorkflowStates() {
        // Implementar precarga de estados de workflow
        log.debug("⚙️ Precargando estados de workflow...");
    }

    /**
     * Monitorea el estado del cache continuamente
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            Map<String, Object> stats = getCacheStatistics();
            @SuppressWarnings("unchecked")
            Map<String, Object> generalStats = (Map<String, Object>) stats.get("_general");
            
            int totalCaches = (Integer) generalStats.get("totalCaches");
            int activeCaches = (Integer) generalStats.get("activeCaches");
            
            health.put("status", activeCaches >= totalCaches * 0.8 ? "UP" : "DEGRADED");
            health.put("totalCaches", totalCaches);
            health.put("activeCaches", activeCaches);
            health.put("availability", String.format("%.1f%%", 
                     (double) activeCaches / totalCaches * 100));
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return health;
    }
}