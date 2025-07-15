package com.desafios.robotcode.controller;

import com.desafios.robotcode.service.HeartbeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class RootController {

    private static final Logger logger = LoggerFactory.getLogger(RootController.class);

    @Autowired
    private HeartbeatService heartbeatService;

    // Endpoint raíz para Railway healthcheck
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        logger.info("Root endpoint accessed at: {}", LocalDateTime.now());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "RobotCode Backend API");
        response.put("version", "2.0.0");
        response.put("status", "running");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("health", "UP");
        
        return ResponseEntity.ok(response);
    }

    // Endpoint simple para Railway healthcheck
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        logger.info("Health endpoint accessed at: {}", LocalDateTime.now());
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "RobotCode Backend");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("version", "2.0.0");
        response.put("environment", "production");
        
        return ResponseEntity.ok(response);
    }

    // Endpoint ping para Railway
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        logger.info("Ping endpoint accessed at: {}", LocalDateTime.now());
        return ResponseEntity.ok("pong");
    }

    // Endpoint de readiness
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        logger.info("Ready endpoint accessed at: {}", LocalDateTime.now());
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ready");
        response.put("service", "RobotCode Backend");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    // Endpoint de heartbeat para mantener la aplicación activa
    @GetMapping("/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat() {
        logger.debug("Heartbeat endpoint accessed at: {}", LocalDateTime.now());
        
        HeartbeatService.HeartbeatStats stats = heartbeatService.getStats();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "RobotCode Backend");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("heartbeat", "active");
        response.put("totalRequests", stats.getTotalRequests());
        response.put("timeSinceLastRequest", stats.getTimeSinceLastRequest());
        
        return ResponseEntity.ok(response);
    }

    // Endpoint de warmup para calentar la aplicación
    @GetMapping("/warmup")
    public ResponseEntity<Map<String, Object>> warmup() {
        logger.info("Warmup endpoint accessed at: {}", LocalDateTime.now());
        
        // Ejecutar algunas operaciones para calentar la aplicación
        try {
            // Simular algunas operaciones de calentamiento
            Thread.sleep(100); // Pequeña pausa para simular trabajo
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "warmed");
            response.put("service", "RobotCode Backend");
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("message", "Application warmed up successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(500).body(Map.of("error", "Warmup interrupted"));
        }
    }
} 