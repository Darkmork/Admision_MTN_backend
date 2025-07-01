package com.desafios.robotcode.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "email_verification_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private boolean isUsed = false;
    
    @Column(nullable = false)
    private boolean isExpired = false;
    
    public EmailVerificationToken(String token, String email, String username, String passwordHash) {
        this.token = token;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
        this.expiresAt = this.createdAt.plusMinutes(10); // Expira en 10 minutos
        this.isUsed = false;
        this.isExpired = false;
    }
    
    public boolean isTokenExpired() {
        return LocalDateTime.now(ZoneOffset.UTC).isAfter(this.expiresAt) || this.isExpired;
    }
    
    public boolean isTokenValid() {
        return !this.isUsed && !this.isTokenExpired();
    }
    
    public void markAsUsed() {
        this.isUsed = true;
    }
    
    public void markAsExpired() {
        this.isExpired = true;
    }
}