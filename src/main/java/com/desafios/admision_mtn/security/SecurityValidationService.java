package com.desafios.admision_mtn.security;

import com.desafios.admision_mtn.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

/**
 * Servicio de validaciones de seguridad avanzadas
 * 
 * Implementa validaciones adicionales para mejorar la seguridad general
 * del sistema más allá de las validaciones básicas de negocio.
 */
@Service
@Slf4j
public class SecurityValidationService {
    
    // Patrones de seguridad
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(\\b(select|insert|update|delete|drop|create|alter|exec|union|script)\\b)|(--)|(;)|(\\*)|(\\||\\|)",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)<script[^>]*>.*?</script>|javascript:|on\\w+\\s*=|<iframe|<object|<embed|<link",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern VALID_EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern CHILEAN_PHONE_PATTERN = Pattern.compile(
        "^\\+56[2-9]\\d{8}$|^[2-9]\\d{8}$"
    );
    
    // Lista de dominios de email temporales/desechables conocidos
    private static final List<String> DISPOSABLE_EMAIL_DOMAINS = List.of(
        "10minutemail.com", "guerrillamail.com", "mailinator.com", 
        "tempmail.org", "yopmail.com", "throwaway.email"
    );
    
    /**
     * Valida la fortaleza de una contraseña
     */
    public PasswordValidationResult validatePasswordStrength(String password) {
        List<String> violations = new ArrayList<>();
        
        if (password == null || password.length() < 8) {
            violations.add("La contraseña debe tener al menos 8 caracteres");
        }
        
        if (password != null && !password.matches(".*[a-z].*")) {
            violations.add("Debe contener al menos una letra minúscula");
        }
        
        if (password != null && !password.matches(".*[A-Z].*")) {
            violations.add("Debe contener al menos una letra mayúscula");
        }
        
        if (password != null && !password.matches(".*\\d.*")) {
            violations.add("Debe contener al menos un número");
        }
        
        if (password != null && !password.matches(".*[@$!%*?&].*")) {
            violations.add("Debe contener al menos un carácter especial (@$!%*?&)");
        }
        
        if (isCommonPassword(password)) {
            violations.add("La contraseña es demasiado común");
        }
        
        boolean isStrong = violations.isEmpty();
        return new PasswordValidationResult(isStrong, violations);
    }
    
    /**
     * Detecta intentos de inyección SQL
     */
    public boolean containsSqlInjection(String input) {
        if (input == null) return false;
        
        boolean detected = SQL_INJECTION_PATTERN.matcher(input).find();
        if (detected) {
            log.warn("🚨 Intento de SQL Injection detectado: {}", 
                    sanitizeForLogging(input));
        }
        return detected;
    }
    
    /**
     * Detecta intentos de XSS
     */
    public boolean containsXss(String input) {
        if (input == null) return false;
        
        boolean detected = XSS_PATTERN.matcher(input).find();
        if (detected) {
            log.warn("🚨 Intento de XSS detectado: {}", 
                    sanitizeForLogging(input));
        }
        return detected;
    }
    
    /**
     * Valida formato de email y detecta emails desechables
     */
    public EmailValidationResult validateEmail(String email) {
        List<String> violations = new ArrayList<>();
        
        if (email == null || email.trim().isEmpty()) {
            violations.add("Email es requerido");
            return new EmailValidationResult(false, violations);
        }
        
        if (!VALID_EMAIL_PATTERN.matcher(email).matches()) {
            violations.add("Formato de email inválido");
        }
        
        String domain = email.substring(email.lastIndexOf('@') + 1).toLowerCase();
        if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
            violations.add("No se permiten emails temporales o desechables");
        }
        
        if (email.length() > 254) {
            violations.add("Email demasiado largo");
        }
        
        boolean isValid = violations.isEmpty();
        return new EmailValidationResult(isValid, violations);
    }
    
    /**
     * Valida formato de teléfono chileno
     */
    public boolean isValidChileanPhone(String phone) {
        if (phone == null) return false;
        return CHILEAN_PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * Valida datos del usuario para detección de anomalías
     */
    public UserValidationResult validateUserData(User user) {
        List<String> violations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Validaciones de seguridad
        if (containsSqlInjection(user.getFirstName()) || 
            containsSqlInjection(user.getLastName())) {
            violations.add("Nombres contienen caracteres no permitidos");
        }
        
        if (containsXss(user.getFirstName()) || 
            containsXss(user.getLastName())) {
            violations.add("Nombres contienen código malicioso");
        }
        
        // Validación de email
        EmailValidationResult emailResult = validateEmail(user.getEmail());
        if (!emailResult.isValid()) {
            violations.addAll(emailResult.violations());
        }
        
        // Validación de teléfono
        if (user.getPhone() != null && !isValidChileanPhone(user.getPhone())) {
            warnings.add("Formato de teléfono no válido para Chile");
        }
        
        // Detección de patrones sospechosos
        if (user.getFirstName() != null && user.getFirstName().equals(user.getLastName())) {
            warnings.add("Nombre y apellido idénticos");
        }
        
        boolean isValid = violations.isEmpty();
        return new UserValidationResult(isValid, violations, warnings);
    }
    
    /**
     * Sanitiza input para prevenir inyecciones
     */
    public String sanitizeInput(String input) {
        if (input == null) return null;
        
        return input
            .replaceAll("<script[^>]*>.*?</script>", "")
            .replaceAll("<[^>]*>", "")
            .replaceAll("javascript:", "")
            .replaceAll("on\\w+\\s*=", "")
            .trim();
    }
    
    /**
     * Verifica si es una contraseña común
     */
    private boolean isCommonPassword(String password) {
        if (password == null) return false;
        
        List<String> commonPasswords = List.of(
            "123456", "password", "123456789", "12345678", "12345",
            "1234567", "admin", "123123", "qwerty", "abc123",
            "password123", "admin123", "123qwe", "12345678"
        );
        
        return commonPasswords.contains(password.toLowerCase());
    }
    
    /**
     * Sanitiza string para logging seguro
     */
    private String sanitizeForLogging(String input) {
        if (input == null) return "null";
        
        return input.length() > 50 ? 
            input.substring(0, 50) + "..." : input;
    }
    
    /**
     * Records para resultados de validación
     */
    public record PasswordValidationResult(
        boolean isValid,
        List<String> violations
    ) {}
    
    public record EmailValidationResult(
        boolean isValid,
        List<String> violations
    ) {}
    
    public record UserValidationResult(
        boolean isValid,
        List<String> violations,
        List<String> warnings
    ) {}
}