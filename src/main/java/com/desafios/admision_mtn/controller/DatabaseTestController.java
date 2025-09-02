package com.desafios.admision_mtn.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RestController
@Slf4j
public class DatabaseTestController {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/api/debug/jdbc-test")
    public String testDirectJdbc(@RequestParam String email) {
        log.info("🧪 JDBC TEST: Testing direct JDBC connection for email: {}", email);
        
        try (Connection connection = dataSource.getConnection()) {
            log.info("🔗 JDBC: Connection obtained: {}", connection.toString());
            
            String sql = "SELECT id, email, first_name, role, active, email_verified FROM users WHERE email = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, email);
                log.info("📤 JDBC: Executing query with email parameter: {}", email);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String result = String.format("✅ JDBC SUCCESS: ID=%d, Email=%s, Role=%s, Active=%s, Verified=%s", 
                            rs.getLong("id"), 
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getBoolean("active"),
                            rs.getBoolean("email_verified"));
                        log.info(result);
                        return result;
                    } else {
                        String result = "❌ JDBC: No results found for email: " + email;
                        log.error(result);
                        return result;
                    }
                }
            }
        } catch (SQLException e) {
            String error = "💥 JDBC ERROR: " + e.getMessage();
            log.error(error, e);
            return error;
        }
    }
    
    @GetMapping("/api/debug/datasource-info")
    public String getDataSourceInfo() {
        try (Connection connection = dataSource.getConnection()) {
            String info = String.format("🗂️ DataSource Info: URL=%s, Schema=%s, Catalog=%s", 
                connection.getMetaData().getURL(),
                connection.getSchema(),
                connection.getCatalog());
            log.info(info);
            return info;
        } catch (SQLException e) {
            String error = "💥 DataSource ERROR: " + e.getMessage();
            log.error(error, e);
            return error;
        }
    }
    
    @GetMapping("/api/debug/generate-hash")
    public String generatePasswordHash(@RequestParam String password) {
        try {
            String hash = passwordEncoder.encode(password);
            log.info("🔒 Generated hash for password '{}': {}", password, hash);
            return String.format("✅ BCrypt Hash: %s", hash);
        } catch (Exception e) {
            String error = "💥 Hash generation ERROR: " + e.getMessage();
            log.error(error, e);
            return error;
        }
    }
    
    @GetMapping("/api/debug/verify-hash")
    public String verifyPasswordHash(@RequestParam String password, @RequestParam String hash) {
        try {
            boolean matches = passwordEncoder.matches(password, hash);
            log.info("🔍 Password '{}' matches hash '{}': {}", password, hash, matches);
            return String.format("✅ Hash verification: %s", matches ? "MATCH" : "NO MATCH");
        } catch (Exception e) {
            String error = "💥 Hash verification ERROR: " + e.getMessage();
            log.error(error, e);
            return error;
        }
    }
}