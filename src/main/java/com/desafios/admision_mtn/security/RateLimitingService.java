package com.desafios.admision_mtn.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio de rate limiting para proteger APIs críticas
 * 
 * Implementa limitación de peticiones por IP/usuario para prevenir abusos
 * y ataques de fuerza bruta en endpoints sensibles.
 */
@Service
@Slf4j
public class RateLimitingService {
    
    // Almacén en memoria para contadores de peticiones
    private final ConcurrentHashMap<String, RequestTracker> requestTrackers = new ConcurrentHashMap<>();
    
    // Configuración de límites por tipo de operación
    private static final int LOGIN_ATTEMPTS_LIMIT = 5; // Por IP en 15 minutos
    private static final int API_REQUESTS_LIMIT = 100; // Por usuario en 1 minuto
    private static final int FILE_UPLOAD_LIMIT = 20; // Por usuario en 1 hora
    private static final int PASSWORD_RESET_LIMIT = 3; // Por email en 1 hora
    
    /**
     * Verifica límites de intentos de login por IP
     */
    public boolean isLoginAllowed(String clientIp) {
        String key = "login:" + clientIp;
        RequestTracker tracker = requestTrackers.computeIfAbsent(key, 
            k -> new RequestTracker(LOGIN_ATTEMPTS_LIMIT, 15));
        
        return tracker.isAllowed();
    }
    
    /**
     * Registra un intento de login fallido
     */
    public void recordFailedLogin(String clientIp) {
        String key = "login:" + clientIp;
        RequestTracker tracker = requestTrackers.computeIfAbsent(key, 
            k -> new RequestTracker(LOGIN_ATTEMPTS_LIMIT, 15));
        
        tracker.recordRequest();
        
        if (!tracker.isAllowed()) {
            log.warn("🚨 IP {} bloqueada por exceso de intentos de login fallidos", clientIp);
        }
    }
    
    /**
     * Verifica límites de peticiones API por usuario
     */
    public boolean isApiRequestAllowed(String userEmail) {
        String key = "api:" + userEmail;
        RequestTracker tracker = requestTrackers.computeIfAbsent(key, 
            k -> new RequestTracker(API_REQUESTS_LIMIT, 1));
        
        return tracker.isAllowed();
    }
    
    /**
     * Registra una petición API
     */
    public void recordApiRequest(String userEmail) {
        String key = "api:" + userEmail;
        RequestTracker tracker = requestTrackers.computeIfAbsent(key, 
            k -> new RequestTracker(API_REQUESTS_LIMIT, 1));
        
        tracker.recordRequest();
    }
    
    /**
     * Verifica límites de subida de archivos por usuario
     */
    public boolean isFileUploadAllowed(String userEmail) {
        String key = "upload:" + userEmail;
        RequestTracker tracker = requestTrackers.computeIfAbsent(key, 
            k -> new RequestTracker(FILE_UPLOAD_LIMIT, 60));
        
        return tracker.isAllowed();
    }
    
    /**
     * Registra una subida de archivo
     */
    public void recordFileUpload(String userEmail) {
        String key = "upload:" + userEmail;
        RequestTracker tracker = requestTrackers.computeIfAbsent(key, 
            k -> new RequestTracker(FILE_UPLOAD_LIMIT, 60));
        
        tracker.recordRequest();
    }
    
    /**
     * Verifica límites de reset de contraseña por email
     */
    public boolean isPasswordResetAllowed(String email) {
        String key = "reset:" + email;
        RequestTracker tracker = requestTrackers.computeIfAbsent(key, 
            k -> new RequestTracker(PASSWORD_RESET_LIMIT, 60));
        
        return tracker.isAllowed();
    }
    
    /**
     * Registra un intento de reset de contraseña
     */
    public void recordPasswordReset(String email) {
        String key = "reset:" + email;
        RequestTracker tracker = requestTrackers.computeIfAbsent(key, 
            k -> new RequestTracker(PASSWORD_RESET_LIMIT, 60));
        
        tracker.recordRequest();
    }
    
    /**
     * Obtiene estadísticas actuales de rate limiting
     */
    @Cacheable(value = "statistics", key = "'rate-limiting-stats'")
    public RateLimitingStats getStatistics() {
        int activeTrackers = requestTrackers.size();
        int blockedIps = 0;
        int blockedUsers = 0;
        
        for (var entry : requestTrackers.entrySet()) {
            if (!entry.getValue().isAllowed()) {
                if (entry.getKey().startsWith("login:")) {
                    blockedIps++;
                } else {
                    blockedUsers++;
                }
            }
        }
        
        return new RateLimitingStats(activeTrackers, blockedIps, blockedUsers);
    }
    
    /**
     * Limpia trackers expirados (mantenimiento)
     */
    public void cleanupExpiredTrackers() {
        LocalDateTime now = LocalDateTime.now();
        requestTrackers.entrySet().removeIf(entry -> {
            RequestTracker tracker = entry.getValue();
            return ChronoUnit.MINUTES.between(tracker.getLastReset(), now) > 
                   tracker.getWindowMinutes() + 5; // Grace period
        });
        
        log.debug("🧹 Limpieza de rate limiting: {} trackers activos", 
                 requestTrackers.size());
    }
    
    /**
     * Clase interna para tracking de peticiones
     */
    private static class RequestTracker {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private final int maxRequests;
        private final int windowMinutes;
        private volatile LocalDateTime lastReset = LocalDateTime.now();
        
        public RequestTracker(int maxRequests, int windowMinutes) {
            this.maxRequests = maxRequests;
            this.windowMinutes = windowMinutes;
        }
        
        public boolean isAllowed() {
            resetIfExpired();
            return requestCount.get() < maxRequests;
        }
        
        public void recordRequest() {
            resetIfExpired();
            requestCount.incrementAndGet();
        }
        
        private void resetIfExpired() {
            LocalDateTime now = LocalDateTime.now();
            if (ChronoUnit.MINUTES.between(lastReset, now) >= windowMinutes) {
                requestCount.set(0);
                lastReset = now;
            }
        }
        
        public LocalDateTime getLastReset() {
            return lastReset;
        }
        
        public int getWindowMinutes() {
            return windowMinutes;
        }
    }
    
    /**
     * Record para estadísticas de rate limiting
     */
    public record RateLimitingStats(
        int activeTrackers,
        int blockedIps,
        int blockedUsers
    ) {}
}