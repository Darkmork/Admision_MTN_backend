package com.desafios.robotcode.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class HeartbeatService {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatService.class);
    
    private final AtomicLong requestCount = new AtomicLong(0);
    private final AtomicLong lastRequestTime = new AtomicLong(System.currentTimeMillis());
    
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Heartbeat interno cada 30 segundos para mantener la aplicación activa
     */
    @Scheduled(fixedRate = 30000) // 30 segundos
    public void internalHeartbeat() {
        try {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastRequest = currentTime - lastRequestTime.get();
            
            logger.debug("💓 Heartbeat interno - Tiempo desde última petición: {}ms", timeSinceLastRequest);
            
            // Actualizar contador de peticiones
            requestCount.incrementAndGet();
            
            // Si han pasado más de 5 minutos sin peticiones, hacer un ping interno
            if (timeSinceLastRequest > 300000) { // 5 minutos
                logger.info("🔄 Aplicación inactiva por {}ms, ejecutando ping interno", timeSinceLastRequest);
                performInternalPing();
            }
            
        } catch (Exception e) {
            logger.warn("⚠️ Error en heartbeat interno: {}", e.getMessage());
        }
    }

    /**
     * Heartbeat de base de datos cada 2 minutos
     */
    @Scheduled(fixedRate = 120000) // 2 minutos
    public void databaseHeartbeat() {
        try {
            logger.debug("🗄️ Heartbeat de base de datos ejecutado");
            // Aquí podrías hacer una consulta simple a la base de datos
            // para mantener la conexión activa
        } catch (Exception e) {
            logger.warn("⚠️ Error en heartbeat de base de datos: {}", e.getMessage());
        }
    }

    /**
     * Ping interno para mantener la aplicación activa
     */
    private void performInternalPing() {
        try {
            // Simular una petición interna para mantener la aplicación activa
            logger.debug("🏓 Ping interno ejecutado");
            
            // Aquí podrías hacer una consulta simple a la base de datos
            // o ejecutar algún servicio interno
            
        } catch (Exception e) {
            logger.warn("⚠️ Error en ping interno: {}", e.getMessage());
        }
    }

    /**
     * Obtener estadísticas del heartbeat
     */
    public HeartbeatStats getStats() {
        return new HeartbeatStats(
            requestCount.get(),
            System.currentTimeMillis() - lastRequestTime.get(),
            LocalDateTime.now()
        );
    }

    /**
     * Registrar una nueva petición
     */
    public void recordRequest() {
        lastRequestTime.set(System.currentTimeMillis());
        requestCount.incrementAndGet();
    }

    /**
     * Clase para estadísticas del heartbeat
     */
    public static class HeartbeatStats {
        private final long totalRequests;
        private final long timeSinceLastRequest;
        private final LocalDateTime timestamp;

        public HeartbeatStats(long totalRequests, long timeSinceLastRequest, LocalDateTime timestamp) {
            this.totalRequests = totalRequests;
            this.timeSinceLastRequest = timeSinceLastRequest;
            this.timestamp = timestamp;
        }

        public long getTotalRequests() { return totalRequests; }
        public long getTimeSinceLastRequest() { return timeSinceLastRequest; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
} 