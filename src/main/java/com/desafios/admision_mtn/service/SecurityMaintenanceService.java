package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.security.RateLimitingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Servicio de mantenimiento de seguridad
 * 
 * Ejecuta tareas programadas para mantener el sistema de seguridad
 * funcionando de manera óptima y eficiente.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityMaintenanceService {
    
    private final RateLimitingService rateLimitingService;
    
    /**
     * Limpia trackers expirados de rate limiting cada 30 minutos
     */
    @Scheduled(fixedRate = 1800000) // 30 minutos = 30 * 60 * 1000 ms
    public void cleanupRateLimitingTrackers() {
        try {
            log.debug("🧹 Iniciando limpieza programada de trackers de rate limiting...");
            rateLimitingService.cleanupExpiredTrackers();
            log.debug("✅ Limpieza de trackers completada");
            
        } catch (Exception e) {
            log.error("❌ Error en limpieza programada de rate limiting", e);
        }
    }
    
    /**
     * Genera reporte de seguridad cada hora
     */
    @Scheduled(fixedRate = 3600000) // 1 hora = 60 * 60 * 1000 ms
    public void generateSecurityReport() {
        try {
            RateLimitingService.RateLimitingStats stats = rateLimitingService.getStatistics();
            
            // Log solo si hay actividad significativa
            if (stats.blockedIps() > 0 || stats.blockedUsers() > 0) {
                log.info("📊 Reporte de Seguridad: {} trackers activos, {} IPs bloqueadas, {} usuarios limitados", 
                        stats.activeTrackers(), stats.blockedIps(), stats.blockedUsers());
            }
            
        } catch (Exception e) {
            log.error("❌ Error generando reporte de seguridad", e);
        }
    }
    
    /**
     * Verifica estado general de seguridad cada 6 horas
     */
    @Scheduled(fixedRate = 21600000) // 6 horas = 6 * 60 * 60 * 1000 ms
    public void verifySecurityHealth() {
        try {
            RateLimitingService.RateLimitingStats stats = rateLimitingService.getStatistics();
            
            // Alertas si hay demasiados bloqueos (posible ataque)
            if (stats.blockedIps() > 50) {
                log.warn("🚨 ALERTA SEGURIDAD: {} IPs bloqueadas - posible ataque en curso", 
                        stats.blockedIps());
            }
            
            if (stats.blockedUsers() > 20) {
                log.warn("🚨 ALERTA SEGURIDAD: {} usuarios limitados - revisar actividad sospechosa", 
                        stats.blockedUsers());
            }
            
            // Verificación de memoria
            if (stats.activeTrackers() > 1000) {
                log.warn("⚠️ ADVERTENCIA: {} trackers activos - considerar ajustar configuración de limpieza", 
                        stats.activeTrackers());
            }
            
            log.info("🔒 Estado de seguridad verificado - Sistema funcionando normalmente");
            
        } catch (Exception e) {
            log.error("❌ Error verificando estado de seguridad", e);
        }
    }
}