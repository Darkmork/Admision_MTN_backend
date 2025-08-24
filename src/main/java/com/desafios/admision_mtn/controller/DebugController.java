package com.desafios.admision_mtn.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Slf4j
// 🔒 SEGURIDAD: Sin @CrossOrigin - usa configuración global de SecurityConfig
public class DebugController {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @GetMapping("/database-info")
    @Transactional
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get current database name
            Query dbQuery = entityManager.createNativeQuery("SELECT current_database()");
            String currentDb = (String) dbQuery.getSingleResult();
            result.put("currentDatabase", currentDb);
            
            // Get current schema
            Query schemaQuery = entityManager.createNativeQuery("SELECT current_schema()");
            String currentSchema = (String) schemaQuery.getSingleResult();
            result.put("currentSchema", currentSchema);
            
            // Get table info
            Query tableQuery = entityManager.createNativeQuery(
                "SELECT schemaname, tablename FROM pg_tables WHERE tablename = 'users'");
            List<?> tables = tableQuery.getResultList();
            result.put("usersTables", tables);
            
            // Get exact row for jorge.gangale@mtn.cl
            Query userQuery = entityManager.createNativeQuery(
                "SELECT id, email, password, updated_at FROM users WHERE email = 'jorge.gangale@mtn.cl'");
            List<?> users = userQuery.getResultList();
            result.put("jorgeUser", users);
            
            // Get total user count
            Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM users");
            Long totalUsers = ((Number) countQuery.getSingleResult()).longValue();
            result.put("totalUsers", totalUsers);
            
        } catch (Exception e) {
            log.error("Error getting database info", e);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/force-fresh-connection")
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public ResponseEntity<Map<String, Object>> getFreshConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Force new transaction context
            entityManager.flush();
            entityManager.clear();
            
            // Force a new query that bypasses all possible caches
            Query freshQuery = entityManager.createNativeQuery(
                "SELECT u.id, u.email, u.password, u.updated_at FROM users u WHERE u.email = 'jorge.gangale@mtn.cl'");
            List<?> freshResult = freshQuery.getResultList();
            result.put("freshUserData", freshResult);
            
            // Get all admin users with fresh connection
            Query allAdminsQuery = entityManager.createNativeQuery(
                "SELECT u.id, u.email, u.password, u.role FROM users u WHERE u.role = 'ADMIN'");
            List<?> allAdmins = allAdminsQuery.getResultList();
            result.put("allAdmins", allAdmins);
            
            // Force commit and reread
            entityManager.flush();
            
            log.info("Fresh connection test completed");
            
        } catch (Exception e) {
            log.error("Error in fresh connection test", e);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
}