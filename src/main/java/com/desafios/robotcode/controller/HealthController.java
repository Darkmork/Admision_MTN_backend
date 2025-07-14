package com.desafios.robotcode.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176", "http://localhost:3000"})
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.info("Health check requested at: {}", LocalDateTime.now());
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "RobotCode Backend");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("version", "2.0.0");
        response.put("environment", "production");
        
        logger.info("Health check response: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        logger.info("Root endpoint accessed at: {}", LocalDateTime.now());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "RobotCode Backend API");
        response.put("version", "2.0.0");
        response.put("status", "running");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    // Endpoint simple para Railway healthcheck
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        logger.info("Ping endpoint accessed at: {}", LocalDateTime.now());
        return ResponseEntity.ok("pong");
    }

    // Endpoint de status más detallado
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        logger.info("Status endpoint accessed at: {}", LocalDateTime.now());
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "RobotCode Backend");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("version", "2.0.0");
        response.put("environment", "production");
        response.put("uptime", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
} 