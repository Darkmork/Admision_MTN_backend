package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176", "http://localhost:5177"})
public class TestController {
    
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @PostMapping("/bcrypt-test")
    public ResponseEntity<Map<String, Object>> testBCrypt(@RequestBody Map<String, String> request) {
        String rawPassword = request.get("password");
        String email = request.get("email");
        
        Map<String, Object> result = new HashMap<>();
        
        // Test 1: Generate new hash
        String newHash = passwordEncoder.encode(rawPassword);
        result.put("newHash", newHash);
        result.put("newHashMatches", passwordEncoder.matches(rawPassword, newHash));
        
        // Test 2: Check user from database if email provided
        if (email != null) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String storedHash = user.getPassword();
                result.put("storedHash", storedHash); // Show full hash for diagnosis
                result.put("storedHashLength", storedHash.length());
                result.put("storedHashMatches", passwordEncoder.matches(rawPassword, storedHash));
                
                // Test character encoding
                result.put("rawPasswordBytes", rawPassword.getBytes().length);
                result.put("storedHashPrefix", storedHash.substring(0, 7));
                
                log.info("BCrypt Test - Email: {}, Raw password: '{}', Stored hash matches: {}", 
                        email, rawPassword, passwordEncoder.matches(rawPassword, storedHash));
            } else {
                result.put("userFound", false);
            }
        }
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/users-sample")
    public ResponseEntity<Map<String, Object>> getUsersSample() {
        Map<String, Object> result = new HashMap<>();
        
        // Get admin users
        var adminUsers = userRepository.findByRole(User.UserRole.ADMIN);
        result.put("adminCount", adminUsers.size());
        
        if (!adminUsers.isEmpty()) {
            User admin = adminUsers.get(0);
            Map<String, Object> adminData = new HashMap<>();
            adminData.put("email", admin.getEmail());
            adminData.put("active", admin.getActive());
            adminData.put("emailVerified", admin.getEmailVerified());
            adminData.put("passwordPrefix", admin.getPassword().substring(0, Math.min(15, admin.getPassword().length())));
            result.put("sampleAdmin", adminData);
        }
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/clear-cache-and-test")
    @Transactional
    public ResponseEntity<Map<String, Object>> clearCacheAndTest(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Force clear all caches
            entityManager.clear();
            entityManager.flush();
            
            // Query directly with native SQL to bypass all caches
            Query nativeQuery = entityManager.createNativeQuery(
                "SELECT password FROM users WHERE email = ?", String.class);
            nativeQuery.setParameter(1, email);
            String dbPassword = (String) nativeQuery.getSingleResult();
            
            result.put("nativeQueryPassword", dbPassword);
            result.put("nativeQueryMatches", passwordEncoder.matches(password, dbPassword));
            
            // Also try with JPA repository after cache clear
            entityManager.clear(); // Clear again just to be sure
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                result.put("repositoryPassword", user.getPassword());
                result.put("repositoryMatches", passwordEncoder.matches(password, user.getPassword()));
                result.put("passwordsMatch", dbPassword.equals(user.getPassword()));
            }
            
            log.info("Cache cleared and tested for email: {}", email);
            
        } catch (Exception e) {
            log.error("Error in cache clear test", e);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
}