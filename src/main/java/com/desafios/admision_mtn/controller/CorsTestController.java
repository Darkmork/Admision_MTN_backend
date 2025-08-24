package com.desafios.admision_mtn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cors-test")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
// 🔒 SEGURIDAD: Sin @CrossOrigin - usa configuración global de SecurityConfig
public class CorsTestController {

    @GetMapping("/simple")
    public ResponseEntity<Map<String, Object>> simpleGet() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CORS test successful - Simple GET request");
        response.put("timestamp", System.currentTimeMillis());
        response.put("method", "GET");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/preflight")
    public ResponseEntity<Map<String, Object>> preflightTest(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CORS test successful - Preflight POST request");
        response.put("timestamp", System.currentTimeMillis());
        response.put("method", "POST");
        response.put("receivedBody", body);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/with-auth")
    public ResponseEntity<Map<String, Object>> withAuthTest(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CORS test successful - With Authorization header");
        response.put("timestamp", System.currentTimeMillis());
        response.put("method", "GET");
        response.put("hasAuth", authorization != null);
        if (authorization != null) {
            response.put("authType", authorization.startsWith("Bearer ") ? "Bearer Token" : "Other");
        }
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/options-test", method = RequestMethod.OPTIONS)
    public ResponseEntity<Map<String, Object>> optionsTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CORS OPTIONS request successful");
        response.put("timestamp", System.currentTimeMillis());
        response.put("method", "OPTIONS");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/headers")
    public ResponseEntity<Map<String, Object>> headersTest(@RequestHeader Map<String, String> headers) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CORS test successful - Headers inspection");
        response.put("timestamp", System.currentTimeMillis());
        response.put("method", "GET");
        response.put("receivedHeaders", headers);
        return ResponseEntity.ok(response);
    }
}