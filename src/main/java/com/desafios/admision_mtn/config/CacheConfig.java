package com.desafios.admision_mtn.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Configuración de cache estratégico para mejorar rendimiento
 * 
 * Optimizado para el sistema de admisión con diferentes estrategias de cache
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache Manager usando Caffeine (alta performance)
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES) // TTL de 30 minutos
            .recordStats() // Para monitoreo
        );
        
        // Configurar caches específicos para el sistema de admisión
        cacheManager.setCacheNames(Arrays.asList(
            "users",              // Cache de usuarios para autenticación
            "evaluations",        // Cache de evaluaciones asignadas
            "applications",       // Cache de aplicaciones por usuario
            "interviews",         // Cache de entrevistas programadas
            "statistics",         // Cache de estadísticas dashboard
            "notifications",      // Cache de notificaciones
            "documents",          // Cache de metadata de documentos
            "workflow-states",    // Cache de estados de workflow
            "validation-rules"    // Cache de reglas de validación
        ));
        
        return cacheManager;
    }
    
    /**
     * Cache de larga duración para datos que cambian poco
     */
    @Bean("longTermCacheManager") 
    public CacheManager longTermCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .initialCapacity(50)
            .maximumSize(500)
            .expireAfterWrite(4, TimeUnit.HOURS) // TTL de 4 horas
            .recordStats()
        );
        
        cacheManager.setCacheNames(Arrays.asList(
            "school-users",    // Personal del colegio
            "document-types",  // Tipos de documentos
            "system-config"    // Configuración del sistema
        ));
        
        return cacheManager;
    }
}