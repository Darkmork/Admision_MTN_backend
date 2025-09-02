package com.desafios.admision_mtn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller temporal para simular endpoints de microservicios
 * Este controlador simula las respuestas que darían los microservicios reales
 */
@RestController
@RequestMapping("/api/microservices-test")
@CrossOrigin(origins = {
    "http://localhost:5173", 
    "http://localhost:5174", 
    "http://localhost:5175", 
    "http://localhost:5176", 
    "http://localhost:5177"
})
public class MicroserviceTestController {

    /**
     * Simular health check del User Service
     */
    @GetMapping("/user-service/health")
    public ResponseEntity<Map<String, String>> userServiceHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "user-service");
        response.put("message", "User Service is running as microservice (SIMULATED)");
        response.put("architecture", "microservices");
        return ResponseEntity.ok(response);
    }

    /**
     * Simular health check del Application Service
     */
    @GetMapping("/application-service/health")
    public ResponseEntity<Map<String, String>> applicationServiceHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "application-service");
        response.put("message", "Application Service is running as microservice (SIMULATED)");
        response.put("architecture", "microservices");
        return ResponseEntity.ok(response);
    }

    /**
     * Simular health check del Evaluation Service
     */
    @GetMapping("/evaluation-service/health")
    public ResponseEntity<Map<String, String>> evaluationServiceHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "evaluation-service");
        response.put("message", "Evaluation Service is running as microservice (SIMULATED)");
        response.put("architecture", "microservices");
        return ResponseEntity.ok(response);
    }

    /**
     * Simular health check del Notification Service
     */
    @GetMapping("/notification-service/health")
    public ResponseEntity<Map<String, String>> notificationServiceHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "notification-service");
        response.put("message", "Notification Service is running as microservice (SIMULATED)");
        response.put("architecture", "microservices");
        return ResponseEntity.ok(response);
    }

    /**
     * Simular health check del API Gateway
     */
    @GetMapping("/gateway/health")
    public ResponseEntity<Map<String, String>> gatewayHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "api-gateway");
        response.put("message", "API Gateway is running (SIMULATED)");
        response.put("architecture", "microservices");
        return ResponseEntity.ok(response);
    }

    /**
     * Simular usuarios del microservicio
     */
    @GetMapping("/user-service/demo-users")
    public ResponseEntity<List<Map<String, Object>>> getDemoUsers() {
        List<Map<String, Object>> users = List.of(
            Map.of(
                "id", 1,
                "name", "Admin Microservicio",
                "email", "admin@microservice.mtn.cl",
                "role", "ADMIN",
                "service", "user-service"
            ),
            Map.of(
                "id", 2,
                "name", "Profesor Microservicio",
                "email", "profesor@microservice.mtn.cl",
                "role", "TEACHER",
                "service", "user-service"
            ),
            Map.of(
                "id", 3,
                "name", "Coordinador Microservicio",
                "email", "coordinador@microservice.mtn.cl",
                "role", "COORDINATOR",
                "service", "user-service"
            )
        );
        
        return ResponseEntity.ok(users);
    }

    /**
     * Simular test de conexión
     */
    @PostMapping("/user-service/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection(@RequestBody(required = false) Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Connection to User Service microservice successful! (SIMULATED)");
        response.put("receivedData", data != null ? data : Map.of());
        response.put("timestamp", Instant.now().toString());
        response.put("service", "user-service (microservice SIMULATION)");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Simular estadísticas del servicio
     */
    @GetMapping("/user-service/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", 3);
        response.put("activeUsers", 3);
        response.put("roles", List.of("ADMIN", "TEACHER", "COORDINATOR"));
        response.put("serviceUptime", "Running since startup (SIMULATED)");
        response.put("architecture", "microservices");
        response.put("database", "users_db (separate from monolith) - SIMULATED");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Simular información del servicio
     */
    @GetMapping("/user-service/info")
    public ResponseEntity<Map<String, Object>> serviceInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "user-service");
        response.put("version", "1.0.0");
        response.put("architecture", "microservices");
        response.put("description", "User management microservice (SIMULATED)");
        response.put("endpoints", List.of(
            "/api/microservices-test/user-service/health",
            "/api/microservices-test/user-service/info",
            "/api/microservices-test/user-service/demo-users"
        ));
        
        return ResponseEntity.ok(response);
    }
}